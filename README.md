# Promethean Agent System

A Clojure-based autonomous agent framework with hierarchical organization, async messaging, and tool-based execution. This system implements a **non-LLM-first** architecture where intelligence emerges from the composition of specialized components rather than relying solely on language models.

## Architecture Overview

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Modules       │    │     Router      │    │     Fuser       │
│ (Sensors)       │───▶│ (Gatekeeper)    │───▶│ (Context Builder)│
│                 │    │                 │    │                 │
│ • Screen        │    │ • Budget Mgmt   │    │ • Data Fusion   │
│ • Vision        │    │ • Hysteresis    │    │ • Compression   │
│ • Novelty       │    │ • Resource Ctrl │    │ • Formatting    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                        │
                                                        ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│     Actions     │◀───│     Agent       │◀───│   World State   │
│   (Executor)    │    │  (Decision)     │    │   (Database)    │
│                 │    │                 │    │                 │
│ • Tool Queue    │    │ • Policy Engine │    │ • Event Store   │
│ • Execution     │    │ • Runtime       │    │ • State Query   │
│ • Results       │    │ • Supervisor    │    │ • History       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         ▲
         │
┌─────────────────┐
│      Tools      │
│  (Capabilities) │
│                 │
│ • LLM (Ollama)  │
│ • System Calls  │
│ • File I/O      │
│ • Network       │
└─────────────────┘
```

## Core Concepts

- **Modules**: Passive sensors that build `WorldState` by observing the environment
- **Router**: Gates sensor activation using budget management and hysteresis to prevent thrashing
- **Fuser**: Produces compact, contextual information for agents by fusing multi-modal data
- **Actions**: Queued operations executed by a comprehensive tool system
- **LLM as Tool**: Language models are just one tool among many, not the primary decision maker
- **Prototypes + Mixins**: Composable behaviors for rapid agent development
- **Hierarchical Organization**: Parent-child agent relationships with supervision trees

## Quick Start

### Prerequisites
- Clojure 1.11+ 
- Java 17+

### Installation
```bash
git clone https://github.com/octave-commons/promethean-agent-system.git
cd promethean-agent-system
```

### Run Demo
```bash
clojure -M -e "(require 'promethean.demo) (promethean.demo/run!)"
```

### Development REPL
```bash
clojure -M:repl
```

## Usage Examples

### Basic Agent Setup
```clojure
(require '[promethean.agent.runtime :as ar]
         '[promethean.tools.registry :as reg]
         '[promethean.llm.ollama :as ollama])

;; Create agent with tool registry
(def registry (reg/make-registry))
(def llm (ollama/make-ollama-driver {:model "llama3.2"}))
(def agent (ar/create-agent registry llm))

;; Run agent tick
(ar/tick! agent)
```

### Custom Module
```clojure
(require '[promethean.modules.core :as modules])

(def-module my-sensor
  {:proto :proto/sensor :enabled true}
  (fn [ctx world module]
    (update world :my-data conj (collect-data))))
```

### Custom Tool
```clojure
(require '[promethean.tool :as tool])

(def-tool my-tool
  {:description "Custom tool description"
   :inputSchema {:type "object" :properties {:param {:type "string"}}}}
  (fn [ctx args]
    {:result "Tool executed successfully"}))
```

## Development

### Code Quality
```bash
# Lint codebase
clj-kondo --lint src/

# Run tests (when implemented)
clojure -M:test
```

### Project Structure
```
src/promethean/
├── agent/          # Agent runtime and supervision
├── actions/        # Action execution and queuing
├── bench/          # Benchmarking and testing scenarios  
├── context/        # Data fusion and context building
├── llm/            # LLM adapters (Ollama, parsing)
├── modules/        # Sensor modules (screen, vision, novelty)
├── mixins/         # Composable behaviors
├── routers/        # Router implementations (stream, budget)
├── runtime/        # Core runtime engine (tick, step)
├── tools/          # Tool system and registry
├── performer/      # Action execution policies
└── proto.clj       # Core protocol definitions
```

## Key Features

- **Async-first design**: All I/O uses core.async channels
- **Hierarchical supervision**: Parent agents manage child lifecycle
- **Budget management**: Configurable limits on tool calls and resource usage
- **Composable architecture**: Mixins and hooks for cross-cutting concerns
- **Event sourcing**: Immutable state changes with historical queries
- **Runtime polymorphism**: Protocol-based extensibility
- **Benchmarking**: Built-in testing for tool choice and routing scenarios

## License

[Add your license here]

## Contributing

[Add contribution guidelines here]
