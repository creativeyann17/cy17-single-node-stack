package main

import "os"

func getEnvOrDefault(env, def string) string {
	value, exists := os.LookupEnv(env)
	if !exists {
		return def
	}
	return value
}

func ifThenElse(condition bool, a interface{}, b interface{}) interface{} {
	if condition {
		return a
	}
	return b
}

func ifEmpty(str string, def string) string {
	if str == "" {
		return def
	}
	return str
}
