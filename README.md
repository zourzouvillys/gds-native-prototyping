# EWOK GDS Buffers

Low level buffer & page management.

The library consists of a low level page access service which performs transfers to from storage (e.g, disk), and a buffer manager which performs page caching, eviction, and a write ahead log service.

The API is exposed through io.ewok.gds.buffers.BufferManager.