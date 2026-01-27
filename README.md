# Promethean Agent System (conversation export)

Created with the assistance of an AI.

This repo is a scaffold that turns the ideas from our conversation into a cohesive, **non-LLM-first** agent runtime:

- Modules (passive sensors) build `WorldState`
- A Router gates sensors (budget + hysteresis)
- A Fuser produces compact context for agents
- Actions are queued and executed by tools
- LLM is a tool (Ollama adapter included)
- Benchmarks test tool choice + router scenarios
- Prototypes + mixins + hooks provide composable behavior

## Quick start

```bash
clojure -M -e "(require 'promethean.demo) (promethean.demo/run!)"
```
