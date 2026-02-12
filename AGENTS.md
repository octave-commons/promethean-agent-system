# Promethean Agent System - Agent Guidelines

A Clojure-based autonomous agent framework with hierarchical organization, async messaging, and tool-based execution.

## Commands

**Development:**
- `clojure -M:repl` - Start REPL for development
- `clojure -M -e "(require 'promethean.demo) (promethean.demo/run!)"` - Run demo
- `clj-kondo --lint src/` - Lint codebase

**Testing (Future):**
- `clojure -M:test` - Run all tests (when implemented)
- `clojure -M:test -n test-name` - Run single test (when implemented)

**Build:**
- No build step required - Clojure source code runs directly

## Code Style

**Naming Conventions:**
- **Namespaces**: `promethean.domain.entity` (e.g., `promethean.llm.ollama`)
- **Functions**: kebab-case (e.g., `def-tool`, `create-world-state`)
- **Variables**: kebab-case (e.g., `world-state`, `agent-config`)
- **Constants**: upper-case with dashes (e.g., `DEFAULT-TIMEOUT`)
- **Private functions**: dash prefix (e.g., `-process-message`)

**Code Organization:**
- **Namespace declaration**: Always include `:require` at top
- **Imports order**: clojure.core → external libraries → internal dependencies
- **File structure**: `ns` declaration → requires → definitions → private helpers

**Function Design:**
- **Pure functions preferred** - no side effects unless required
- **Explicit arguments** - avoid implicit `*` vars when possible
- **Return values** - always return meaningful result (nil for no-op)
- **Documentation** - docstrings for all public functions
- **Spec validation** - use `clojure.spec.alpha` for input validation

**Async Patterns:**
- **core.async channels** for all inter-component communication
- **Go blocks** for concurrent operations
- **>!! and <!!** for blocking channel operations
- **Pipelines** use `pipeline` and `pipeline-async` for transformation

**Error Handling:**
- **Prefer `ex-info`** over `Exception.` for structured errors
- **Return error maps** instead of throwing when recoverable
- **Use `try`/`catch`** only at boundaries (tool execution, external I/O)
- **Graceful degradation** - components operate in degraded mode when dependencies fail

**Component Architecture:**
- **Prototypes**: Use `deftype` with protocol implementations
- **Mixins**: Composable behaviors added via mixins
- **Hooks**: Lifecycle hooks (`:before`, `:after`, `:around`) for cross-cutting concerns
- **Resource isolation**: Strong isolation between agents

**State Management:**
- **Immutable data structures** always
- **Event sourcing**: All state changes as immutable events
- **Atom references** for mutable state when absolutely necessary
- **Datomic-like patterns**: as-of queries for historical state

**Tool Definition:**
```clojure
(def-tool tool-name
  {:description "Tool description"
   :inputSchema {...}
   :hooks {:before [...] :after [...]}}
  (fn [ctx args] ...))
```

**Module Definition:**
```clojure
(def-module module-name
  {:proto :proto/sensor :enabled true}
  (fn [ctx world module] ...))
```

**Resource Management:**
- **Budget limits**: Configurable limits on tool calls, memory usage
- **Timeout handling**: Always include timeouts for external operations
- **Resource cleanup**: Use `with-open` for external resources
- **Supervisor patterns**: Parent agents supervise and manage child agents

## Architecture Principles

**Async-First Design:**
- All I/O operations must be non-blocking
- Use channels for inter-component communication
- Separate processing from external calls

**Hierarchical Organization:**
- Parent-child agent relationships
- Supervision trees for fault tolerance
- Resource delegation through hierarchy

**Protocol-Based Design:**
- Extensible interfaces for all components
- Protocol implementations for different capabilities
- Runtime polymorphism over static dispatch

**Model Tiering:**
- Different capability levels with cost tracking
- Progressive enhancement of agent abilities
- Cost-aware decision making

## Testing Guidelines (When Implemented)

**Test Structure:**
- Use `clojure.test` framework
- Test files: `test/promethean/domain/entity_test.clj`
- Fixtures: `(use-fixtures :each fixture-fn)` for setup/teardown

**Testing Patterns:**
- **Unit tests**: Test pure functions extensively
- **Integration tests**: Test component interactions
- **Async testing**: Use `async/<!` and `async/>!` with timeouts
- **Property-based testing**: Use `test.check` for complex logic

**Test Data:**
- Use `generators` for test data when appropriate
- Isolate test data from production data
- Clean up external resources in fixtures

## Development Workflow

**Before Starting:**
1. Ensure clean working directory
2. Start REPL for interactive development
3. Run `clj-kondo --lint src/` to check code quality

**During Development:**
1. Write/modify code in `src/`
2. Test in REPL incrementally
3. Run linting frequently
4. Commit when functionality is complete

**Before Committing:**
1. Run `clj-kondo --lint src/`
2. Test manual workflows in REPL
3. Ensure demo still runs: `clojure -M -e "(require 'promethean.demo) (promethean.demo/run!)"`

## Performance Guidelines

**Channel Usage:**
- Use buffered channels for high-throughput streams
- Prefer transducers for data transformation pipelines
- Avoid channel leakage - always close unused channels

**Memory Management:**
- Use reducers for large collections
- Implement object pools for frequently created objects
- Monitor GC pressure with tools like `clj-memory-meter`

**Concurrency Patterns:**
- Limit parallelism with semaphore when necessary
- Use `pipeline` for CPU-bound work
- Implement backpressure for producer-consumer systems

## Security Considerations

**Tool Execution:**
- Validate all inputs before tool execution
- Sandboxing for external tool execution
- Resource limits to prevent DoS attacks

**Agent Isolation:**
- Separate namespaces for different agent capabilities
- No shared mutable state between agents
- Audit logging for all agent actions

**External Communication:**
- Secure communication channels for distributed agents
- Authentication and authorization for agent interactions
- Rate limiting for external API calls
## RELEVANT SKILLS
These skills are configured for this directory's technology stack and workflow.

### clojure-namespace-architect
Resolves Clojure namespace-path mismatches and classpath errors with definitive path conversion

### clojure-quality
Auto-fix Clojure delimiters and validate syntax with OpenCode tools.

### clojure-syntax-rescue
Protocol to recover from Clojure/Script syntax errors, specifically bracket mismatches and EOF errors.

### create-pm2-clj-config
Create new pm2-clj ecosystem configuration files from scratch or templates for PM2 process management

### create-pm2-ecosystem
Create new PM2 ecosystem configuration files for the clobber-based system with proper defapp definitions

### git-safety-check
Protocol to ensure safe git operations and avoid detached HEAD or dirty commits.

### github-integration
Perform GitHub operations across all tracked repositories in orgs/**, including issue/PR management, repository synchronization, and automation workflows

### pm2-process-management
Start, stop, restart, and manage PM2 processes using the ecosystem-based configuration system

### render-pm2-clj-config
Render pm2-clj ecosystem files to JSON for validation and debugging without starting processes

### submodule-ops
Make safe, consistent changes in a workspace with many git submodules under orgs/**

### test-preservation
Protocol to forbid deleting or skipping tests to make builds pass.

### testing-bun
Set up and write tests using Bun's built-in test runner for maximum performance and TypeScript support

### testing-clojure-cljs
Set up and write tests for Clojure and ClojureScript projects using cljs.test, cljs-init-tests, and shadow-cljs

### testing-e2e
Write end-to-end tests that verify complete user workflows and critical system paths across the full stack

### testing-general
Apply testing best practices, choose appropriate test types, and establish reliable test coverage across the codebase

### testing-integration
Write integration tests that verify multiple components work together correctly with real dependencies

### testing-nx
Configure and run tests across multiple projects using Nx affected detection for efficient workspace testing

### testing-typescript-ava
Set up and write tests using Ava test runner for TypeScript with minimal configuration and fast execution

### testing-typescript-vitest
Set up and write tests using Vitest for TypeScript projects with proper configuration and TypeScript support

### testing-unit
Write fast, focused unit tests for individual functions, classes, and modules with proper isolation and mocking

### work-on-in_progress-task
Execute the best next work for a task currently in `in_progress`.

### work-on-todo-task
Execute the best next work for a task currently in `todo`.

### workspace-lint
Lint all TypeScript and markdown files across the entire workspace, including all submodules under orgs/**

