# Scheduling Fibers

A Fiber is a library for managing high performance async execution contexts that does not block on external io, lock, or otherwise stall significantly.

A Fiber instance gets dispatched from FiberGuest.start(), FiberGuest.signal(), or FiberGuest.reenter().

- start is called the first time this fiber is getting scheduled.  think of it as your main().
- signal gets called when there is an external event that has been requested.  disk io, network, timer, message from another fiber, etc.
- reenter gets called when there is a callback that needs to be executed.

- service() is called to dispatch a seperate thread which wishes to make an API call.

## OS Threads

The scheduler is responsible for calculating when and where to run Fibers.  It has the insight from metrics on disk, network, CPU, and memory to make the best possible decision as well as provide feedback for rate limiting network io, CPU & disk.  The scheduler can also be given dependencies for completion, allowing better selection for process selection.

Under high loads, the goal is to never context switch, and spend almost all time processing requests.  This requires placing some constraints on implementations to avoid GC pressure and ensure threads never block on IO.

Threads should be pinned to CPUs, and the CPUs isolated from having other jobs scheduled on them. memory should be preallocated using huge pages, aligned to block size boundaries, and locked in place.

The scheduler should use its own CPU where possible.
 
the ewok runtime host provides:

- Buffer Pools (Pages & Protection)
- Clock Time & Scheduler Ticks
- Ambient State & Work Contexts
- Cancellation Tokens
- Async Callback Scheduling
- Stream Backpressure
- Logging
- Metrics / Perf
- Network IO
- Disk IO
- Async FD/path operations
- Encryption
- IO DMA
- Inter Fiber Signals
- Inter Fiber Channels
- Inter Fiber Memory
- Fiber Dependency Scheduling
- Service Registry (iface based marshalling)
- Scheduling Priorities
- API timeout handlers

# Scheduling Theory

## Rapid Fiber Switching

A fiber often needs to interact with other fibers, which may not be running at the time.  one options is one when the target fiber it is not running could either mean sleeping the caller, waking up the target, dispatching, and then putting it back to sleep and waking up the caller again.

a better option is to perform "rapid fiber switching", which essentially switches into the target fiber, and continues processing as if the target fiber got scheduled itself.

```

int start() {

  host.lookup("theo");

}

```


## Priorities

A Fiber has a priority, which is a weight relative to other fibers.




## Run to completion

Cache benefits arise when work is resolved quickly.
 



