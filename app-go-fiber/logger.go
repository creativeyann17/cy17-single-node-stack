package main

import (
	"fmt"
	"log/slog"
	"runtime"
	"strings"

	"gopkg.in/natefinch/lumberjack.v2"
)

type LogLevel string

const (
	DEBUG LogLevel = "debug"
	INFO  LogLevel = "info"
	ERROR LogLevel = "error"
)

var LOG_LEVEL = LogLevel(getEnvOrDefault("LOG_LEVEL", string(DEBUG)))

type Logger struct {
	fileLogger *slog.Logger
}

func NewLogger() *Logger {

	var opts = &slog.HandlerOptions{
		Level: ifThenElse(LOG_LEVEL == DEBUG, slog.LevelDebug, slog.LevelInfo).(slog.Level),
	}

	var output = &lumberjack.Logger{
		Filename:   "./logs/app.log",
		MaxSize:    10,   // MB
		MaxBackups: 10,   // files
		MaxAge:     10,   // days
		Compress:   true, // disabled by default
	}

	fileLogger := slog.New(slog.NewJSONHandler(output, opts))
	slog.SetLogLoggerLevel(opts.Level.Level())

	return &Logger{fileLogger: fileLogger}
}

func formatLogMessage(msg string, a ...any) string {
	return fmt.Sprintf("%s %s", logger.caller(), fmt.Sprintf(msg, a...))
}

func (logger *Logger) Info(msg string, a ...any) {
	v := formatLogMessage(msg, a...)
	slog.Info(v)
	logger.fileLogger.Info(v)
}

func (logger *Logger) Error(msg string, a ...any) {
	v := formatLogMessage(msg, a...)
	slog.Error(v)
	logger.fileLogger.Error(v)
}

func (logger *Logger) Debug(msg string, a ...any) {
	v := formatLogMessage(msg, a...)
	slog.Debug(v)
	logger.fileLogger.Debug(v)
}

func (l *Logger) caller() string {
	_, fullFile, line, ok := runtime.Caller(3)
	if ok {
		splitPath := strings.Split(fullFile, "/")
		file := splitPath[len(splitPath)-1]
		return fmt.Sprintf("%s:%d", file, line)
	} else {
		return ""
	}
}
