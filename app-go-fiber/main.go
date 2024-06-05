package main

import (
	"fmt"
	"log"
	"os"
	"os/signal"
	"runtime"
	"runtime/debug"
	"slices"
	"syscall"
	"time"

	"github.com/gofiber/fiber/v2"

	_ "app-go-fiber/docs"

	"github.com/mvrilo/go-redoc"
	fiberredoc "github.com/mvrilo/go-redoc/fiber"
)

type Status struct {
	Status string `json:"status" example:"OK"`
}

var logger = NewLogger()
var publicRoutes = []string{"/actuator/health", "/favicon.ico"}
var PORT = getEnvOrDefault("PORT", "8080")
var start = time.Now()
var CPU_COUNT = runtime.GOMAXPROCS(0)

//go:generate swag init

// @title Fiber Example API
// @version 1.0
// @description Fiber example for openapi spec generation
// @host localhost:8000
// @BasePath /
func main() {

	doc := redoc.Redoc{
		Title:       "Example API",
		Description: "Example API Description",
		SpecFile:    "./docs/swagger.json",
		SpecPath:    "/docs/swagger.json",
		DocsPath:    "/docs",
	}

	app := fiber.New(fiber.Config{
		Prefork:       true, // probably great in multi-core environments
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
	api.Get("/param/:param", func(c *fiber.Ctx) error {
		param := c.Params("param")
		return c.SendString("Param: " + param)
	})
	api.Get("/error", func(c *fiber.Ctx) error {
		p := c.Query("panic")
		if p == "true" {
			panic("Panic!")
		} else {
			return fiber.NewError(fiber.StatusNotFound, "Not found stuff")
		}
	})

	app.Hooks().OnListen(func(data fiber.ListenData) error {
		if fiber.IsChild() {
			return nil
		}
		logger.Info(fmt.Sprintf("App is starting with Cores: %d (total available: %d)", CPU_COUNT, runtime.NumCPU()))
		logger.Info(fmt.Sprintf("Current log level: %s", LOG_LEVEL))
		logger.Info("HTTP server started on port: " + data.Port)
		logger.Info("App started in %dus", time.Since(start).Microseconds())
		return nil
	})

	shutdownHook(app)
	app.Use(fiberredoc.New(doc))
	err := app.Listen(":" + PORT)
	if err != nil {
		log.Fatal(err)
	}
}

// ShowAccount godoc
// @Summary      Show an account
// @Description  get string by ID
// @Tags         actuator
// @Accept       json
// @Produce      json
// @Param        id   path      int  true  "Account ID"
// @Success      200  {object}  Status
// @Failure      400  {string}  string
// @Failure      404  {string}  string
// @Failure      500  {string}  string
// @Router       /actuator/health/{id} [get]
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
		var msg = fmt.Sprintf("[%15s] %s %d %s %dus", ifEmpty(c.IP(), "0.0.0.0"), c.Method(), code, c.Path(), time.Since(start).Microseconds())
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
	signal.Notify(c, syscall.SIGTERM, syscall.SIGINT)
	go func() {
		<-c
		logger.Info("Shutting down the server...")
		if err := app.ShutdownWithTimeout(5 * time.Second); err != nil {
			logger.Info("Error shutting down the server: %v", err)
		}
	}()
}
