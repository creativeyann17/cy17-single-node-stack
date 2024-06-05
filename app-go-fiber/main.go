package main

import (
	"fmt"
	"log"
	"os"
	"os/signal"
	"runtime"
	"runtime/debug"
	"slices"
	"time"

	"github.com/gofiber/fiber/v2"
)

type Status struct {
	Status string `json:"status"`
}

var logger = NewLogger()
var publicRoutes = []string{"/actuator/health", "/favicon.ico"}

func main() {

	app := fiber.New(fiber.Config{
		CaseSensitive: true,
		StrictRouting: true,
		ProxyHeader:   "X-Real-IP",
	})

	app.Use(globalHandler())

	actuators := app.Group("/actuator")
	actuators.Get("/health", healthHandler)

	api := app.Group("/api/v1")
	api.Get("/hello", func(c *fiber.Ctx) error {
		return c.SendString("Hello, World!")
	})
	api.Get("/error", func(c *fiber.Ctx) error {
		p := c.Query("panic")
		if p == "true" {
			panic("Panic!")
		} else {
			return fiber.NewError(fiber.StatusNotFound, "Not found stuff")
		}
	})

	logger.Info(fmt.Sprintf("App is starting with Cores: %d", runtime.NumCPU()))
	logger.Info(fmt.Sprintf("Current log level: %s", LOG_LEVEL))

	shutdownHook(app)

	log.Fatal(app.Listen(":8080"))
}

func healthHandler(c *fiber.Ctx) error {
	health := Status{Status: "OK"}
	return c.JSON(health)
}

func logRequest(c *fiber.Ctx, start time.Time, err error) {
	code := c.Response().StatusCode()
	if e, ok := err.(*fiber.Error); ok {
		code = e.Code
	}

	if !slices.Contains(publicRoutes, c.Path()) || code == fiber.StatusInternalServerError {
		var msg = fmt.Sprintf("[%15s] %s %d %s %d ms", c.IP(), c.Method(), code, c.Path(), time.Since(start).Milliseconds())
		logger.Info(msg)
	}
}

func globalHandler() fiber.Handler {
	return func(c *fiber.Ctx) error {

		start := time.Now()

		defer func() {
			if r := recover(); r != nil {
				err := fmt.Sprintf("%v", r)
				c.Status(fiber.StatusInternalServerError).SendString("Internal Server Error")
				logRequest(c, start, fmt.Errorf(err))
				logger.Error(string(debug.Stack()))
			}
		}()

		err := c.Next()
		logRequest(c, start, err)
		return err
	}
}

func shutdownHook(app *fiber.App) {
	c := make(chan os.Signal, 1)
	signal.Notify(c, os.Interrupt)
	go func() {
		<-c
		logger.Info("Shutting down the server...")
		if err := app.ShutdownWithTimeout(5 * time.Second); err != nil {
			logger.Info("Error shutting down the server: %v", err)
		}
	}()
}
