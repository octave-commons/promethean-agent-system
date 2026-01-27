'# Core Framework Specification

**Status**: DRAFT  
**Version**: 0.1.0  
**Last Updated**: 2026-01-25  
**Dependencies**: None (foundation spec)

## Overview

This specification defines the core framework components that underpin the entire ollama benchmark system, including the Ollama client, tool registry, event sourcing, and resource management systems.

## 1. Ollama Client Interface

### Purpose
Provide a unified, non-streaming interface to Ollama's /api/chat endpoint for both benchmark runners and production agents.

### Requirements
- **HTTP-based**: Java HttpClient with configurable timeouts
- **JSON handling**: Request/response serialization with cheshire
- **Tool support**: OpenAI-style tool schema transmission
- **Error handling**: Structured exception handling with detailed error context
- **Configuration**: Default host (http://localhost:11434), configurable model, options

### Interface
```clj
;; Core client interface
(defprotocol OllamaClient
  (chat! [{:keys [host model messages tools options timeout-ms think]}]
    "Calls Ollama /api/chat (non-stream)
   Returns decoded JSON response map."))

;; Tool schema format (OpenAI-compatible)
{:type "function"
 :function {:name "tool_name"
            :description "Tool description"
            :parameters {...JSON-Schema...}}}
```

## 2. Tool Registry and Validation

### Purpose
Centralized tool definition, registration, validation, and schema generation system supporting both production agents and benchmark environments.

### Requirements
- **Dynamic registration**: Tools can be registered at runtime
- **Schema generation**: Automatic OpenAI-compatible JSON schema from Clojure parameter definitions
- **Validation**: clojure.spec.alpha-based argument validation
- **Type coercion**: Automatic conversion between Clojure data types and JSON
- **Registry management**: Clear, list, and lookup operations

### Core Components
```clj
;; Tool definition structure
(defprotocol ToolRegistry
  (register-tool! [tool] "Register tool by name")
  (tools [] "Return all registered tools")
  (tool-by-name [name] "Lookup tool by name")
  (clear-tools! [] "Clear all tools"))

;; Tool validation
(defprotocol ToolValidator
  (validate-tool-call [{:keys [name arguments]}] "Validate tool call against spec")
  (coerce-arguments [arguments] "Convert JSON/string args to Clojure map"))
  (tool->ollama-schema [tool] "Generate OpenAI-compatible schema"))
```

## 3. Event Sourcing and Logging

### Purpose
Provide durable, append-only event logging for state reconstruction, debugging, and resumable execution across benchmark runs and agent operations.

### Requirements
- **JSONL format**: Append-only structured logging
- **Event types**: Agent lifecycle, tool calls, state changes, errors
- **Atomic writes**: Thread-safe file operations
- **Replay capability**: Ability to reconstruct state from event stream
- **Performance**: Minimal overhead, async-friendly

### Event Schema
```clj
;; Core event types
{:event/type "agent/spawned"
 :agent/id "agent-identifier"
 :timestamp unix-ms
 :initial-state {...}}

{:event/type "tool/call-started"
 :agent/id "agent-identifier"
 :tool/name "tool-name"
 :call/id "unique-call-id"
 :arguments {...}
 :timestamp unix-ms}

{:event/type "tool/result"
 :agent/id "agent-identifier"
 :call/id "unique-call-id"
 :tool/name "tool-name"
 :ok true/false
 :value result-or-error-details
 :timestamp unix-ms}
```

## 4. Message Bus Architecture

### Purpose
Provide asynchronous communication between framework components (agents, benchmarks, tools) with structured routing and filtering capabilities.

### Requirements
- **Channel-based**: core.async channels for message passing
- **Event routing**: Type-based message filtering and delivery
- **Broadcast support**: One-to-many message distribution
- **Error isolation**: Channel-level error handling without cross-contamination
- **Performance**: Low-latency inter-component communication

### Message Types
```clj
;; Message protocol
(defprotocol BusMessage
  (message-type [] "Returns message type keyword")
  (target [] "Returns target recipient(s)")
  (payload [] "Returns message payload")
  (metadata [] "Returns optional metadata"))

;; Core message types
:agent/lifecycle     ; Agent start/stop/status
:tool/request       ; Tool execution requests  
:tool/result        ; Tool execution results
:bench/control      ; Benchmark control signals
:system/state       ; System state changes
```

## 5. File Locking and Resource Management

### Purpose
Provide exclusive access to shared resources (files, tools, models) with conflict detection, TTL support, and deadlock prevention.

### Requirements
- **Exclusive locks**: Write and read lock modes with owner tracking
- **TTL support**: Time-based lock expiration with heartbeat
- **Conflict resolution**: Structured conflict detection and escalation
- **Deadlock prevention**: Consistent locking order and timeout handling
- **Resource tracking**: Central registry of active locks and ownership

### Lock Operations
```clj
;; Lock service interface
(defprotocol LockService
  (acquire! [{:keys [path mode owner ttl-ms]}] "Acquire lock")
  (release! [{:keys [path owner]}] "Release lock")
  (heartbeat! [{:keys [path owner]}] "Extend lock TTL")
  (get-status [path] "Get lock status and owner")
  (force-release! [path] "Administrative override"))
```

## 6. Configuration Management

### Purpose
Centralized configuration system with environment variable support, default values, and runtime override capabilities.

### Requirements
- **Environment integration**: Support for .env files and system properties
- **Type safety**: Configuration validation with helpful error messages
- **Runtime updates**: Ability to modify configuration without restart
- **Namespacing**: Hierarchical configuration with dot-notation access
- **Secrets handling**: Secure management of sensitive configuration

### Configuration Structure
```clj
;; Configuration hierarchy
{:ollama/default-host "http://localhost:11434"
 :ollama/default-timeout-ms 300000
 :tools/validation-enabled true
 :events/log-path "logs/events.jsonl"
 :locks/default-ttl-ms 60000
 :agents/max-concurrent 10
 :benchmarks/default-steps 4}
```

## Implementation Notes

### Error Handling Strategy
- **Graceful degradation**: Components should operate in degraded mode when dependencies fail
- **Context preservation**: All errors include relevant state and context
- **Recovery mechanisms**: Automatic retry with exponential backoff where appropriate
- **User-friendly errors**: Clear error messages with actionable suggestions

### Performance Considerations
- **Lazy initialization**: Components initialize on first use
- **Resource pooling**: Reuse HTTP clients and connections where possible
- **Async by default**: All I/O operations should be non-blocking
- **Memory efficiency**: Avoid unnecessary object creation in hot paths

### Security Considerations
- **Input validation**: All external inputs validated before processing
- **Path traversal prevention**: File operations restricted to allowed directories
- **Resource limits**: Configurable limits on tool calls, file handles, etc.
- **Audit logging**: Security-relevant events logged with timestamps

## Testing Strategy

### Unit Testing
- Interface compliance testing for all protocols
- Error condition simulation and validation
- Mock implementations for external dependencies
- Property-based testing for configuration variations

### Integration Testing
- End-to-end workflow testing with real Ollama instances
- Concurrent operation testing with multiple agents/benchmarks
- Resource contention scenarios (file locks, tool conflicts)
- Error recovery and resilience testing

### Validation Testing
- Schema validation correctness with comprehensive test cases
- Tool registration and lookup edge cases
- Message routing and delivery guarantees
- Lock service correctness under various failure modes

## Migration and Compatibility

### Version Compatibility
- **Semantic versioning**: Use SemVer for all public interfaces
- **Backward compatibility**: Maintain compatibility for at least one minor version
- **Migration path**: Clear upgrade procedures with data migration support
- **Deprecation policy**: Gradual deprecation with advance notice

### Extensibility Points
- **Plugin architecture**: Support for custom tool providers
- **Custom event types**: Allow registration of domain-specific events
- **Alternative lock providers**: Support for distributed lock services
- **Custom benchmarks**: Framework for user-defined benchmark types