package main

import (
	"encoding/json"
	"net/http"

	"github.com/go-chi/chi/v5"
	"github.com/go-chi/chi/v5/middleware"
)

type Status struct {
  Status string `json:"status"`
}

func main() {
	r := chi.NewRouter()

	r.Use(middleware.Logger)
  r.Use(middleware.Recoverer)

	r.Get("/api/v1/hello", func(w http.ResponseWriter, r *http.Request) {
		w.Write([]byte("Hello World!"))
	})

  r.Get("/api/v1/param/{param}", func(w http.ResponseWriter, r *http.Request) {
		w.Write([]byte(r.PathValue("param")))
	})

  GetHead(r, "/actuator/health", healthHandler)

	http.ListenAndServe(":8080", r)
}

func healthHandler(w http.ResponseWriter, r *http.Request) {
  var health = Status{Status: "OK"}
  w.Header().Set("Content-Type", "application/json")
  if err := json.NewEncoder(w).Encode(health); err != nil {
    w.WriteHeader(http.StatusInternalServerError)
    w.Write([]byte("internal server error"))
  }
}

func GetHead(r chi.Router, pattern string, h http.HandlerFunc) {
	r.Get(pattern, h)
	r.Head(pattern, h)
}
