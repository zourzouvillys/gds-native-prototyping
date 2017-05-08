# EWOK GDS Storage API

Low level block device access, object, buffer & page management.

The library consists of a low level page access service which performs transfers to from storage (e.g, disk), and a buffer manager which performs page caching, eviction, transactions, and a write ahead log service.

# Work Contexts

A primary goal of GDS is to never block, and avoid locking wherever possible - to let the consumer threads decide how to spend time waiting on IO.

To this end, any API calls which may require disk or network activity to occur before they can complete use completion futures or callbacks rather than blocking.

To ensure healthy resource management, avoiding overloading, and taking on too much work, almost all APIs take a WorkContext.  WorkContext is the scope that APIs are used, that generally maps either to clients or maintenance.  A WorkContext provides keeps track of resource usage, and provides buffers to components that need them.  If a WorkContext is starting to be too aggressive in its resource consumption or disk IO, it can be throttled back or killed. 
 
A WorkContext is assigned a given number of IOPS, a priority, and memory usage when being created.  This can be used to ensure that too much work can not be taken on.

A WorkContext is also allocated to a specific thread pool.  Async callbacks are always dispatched within the same pool.  This allows different thread pools to be used for different work contexts, thus creating different work priorities.  Pools can be pinned to specific CPUs, and the threads have their IO priority set.

Note that io priorities can affect shared buffer reads.  If a low priority task requests a page, and the high priority one piggybacks onto it, it can feel the wrath by being delayed.  To work around this, a page being requested by a higher priority context will adopt the request if it has not already been submitted to the kernel. 

Likewise, for shared buffers, when a page is needed by both it will be pinned to both.  When borrowed from another pool, it will be tracked and the buffer slot can be reused for something else.  Once the owning pool is going to release it, the other pool can chose if it wants to adopt it or not, potentially pushing out other buffers in the process.

Some maintenance work is performed during normal operation.  For example, if an xmin/xmax is encountered that does not have the transaction status set, a lookup occurs to set it on demand.  Likewise, FSM or visibility updates.  In these cases, the work context is not penalised, and instead the operation performed using the "maintenance work" context.  CPU/work time is credited to adjust for delays in completing the requested work because of these tasks, too.

## Consistency

When a page larger than the disk block size is written to disk, there is no guarantee of atomicity.  A partial page write can occur if the power fails while performing a page write.  This is known as a "fragmented block".

To combat this, GDS creates a copy of a page before writing to disk, and waits until it has been flushed before writing. Once the dirty page has been flushed, then the journal entry can be removed/reused.  The clean page may be copied using transferTo to improve performance, as the page does not need to be copied into userspace.

Another option is to use a filesystem which supports atomic writes.  In these cases, full page writes are guaranteed so there is no need to perform page copies.

If a consumer doesn't care about potentially broken page because it can retrieve it from somewhere else (e.g, a backup replica), then page backups are not needed.



# Page Blocks

## Page Layout

All pages within a single object must be the same size.  The sizes a page can be are:

| 0 | 512   | 000 |
| 1 | 1024  | 001 |
| 2 | 2056  | 010 |
| 3 | 4096  | 011 |
| 4 | 8192  | 100 |
| 5 | 16348 | 101 |
| 6 | 32768 | 110 |
| 7 | 65536 | 111 |

### Page Header

Page header is fixed at 22 bytes.

| 0  | layout    | uint8  | first 4 bits are version (currently always 1). |
|    |           |        | last 4 bits are page size. see page size table above. |
| 1  | flags     | uint8  | flag bits. see flag table below. |
| 2  | checksum  | uint16 | page checksum |
| 4  | prune_xid | uint32 | oldest unpruned xmax on page, or zero if none. |
| 8  | lsn       | uint64 | next byte in WAL after last write to this page |
| 16 | lower     | uint16 | offset to start of free space |
| 18 | upper     | uint16 | offset to end of free space |
| 20 | special   | uint16 | offset to special space (may be zero) |

#### Page Header Flags

| 0 ||
| 1 ||
| 2 ||
| 3 ||
| 4 ||
| 5 ||
| 6 ||
| 7 ||


#### Page Checksum

The checksum is calculated as CRC32C checksum over the whole page, with bytes 8-10 (the checksum field itself) set to zero.

Additionally, the page number is added to the checksum as a long.

The checksum value is stored as a 16 bit integer calculated by modding by 65535, then adding one.  This means we never have a checksum value of zero.

#### Empty Pages

Data in the freespace does not need to be set to zeros, however for security of deleted data and compression, it is recommended.

### Item Header

For each tuple in the page there is an item header.

| 0 | offset | uint16 | offset of the tuple from the start of the page. |
| 2 | length | uint16 | length of the tuple in bytes |


# Performance

GDS buffer manager can be configured with target IOPS and throughput.  This ensures that it does not overwhelm the io queue, and keeps work at a suitable state.

GDS is not aware of other disk IO, so the configured values are just limits for GDS itself.  e.g:

- Block Size: 4K
- Read IOPS/Throughput: 10K/100MB
- Write IOPS/Throughput: 10K/100MB
- Queue Depth: 64
- Read Ahead: 4K

## Kernel Buffering

GDS buffering can be used for either full userspace, no kernel page caching all the way through to 100% kernel buffering and no local.  To use kernel buffering instead, keep the buffer sizes very small, increase kernel caching, and immediate flushing.

The result in GDS will rely entirely on the kernel for page fetching.  Pages will be evicted quickly from GDS.


## NUMA Awareness

Data retrieved from disk is always submitted from the IO threads, but written directly to the buffer provided by the consumer.  Be aware of cross-NUMA behaviour.

## File Allocation
 
GDS can preallocate data file space on disk, which avoids fragmentation using fallocate().  This also considerably speeds up page writes for new pages.

## OS configuration

	echo deadline > /sys/block/sda/queue/scheduler
	#OR...
	#echo noop > /sys/block/sda/queue/scheduler 
	echo 0 > /sys/class/block/sda/queue/rotational
	echo 8 > /sys/class/block/sda/queue/read_ahead_kb

	vm.dirty_ratio = 15
	vm.dirty_background_ratio = 5
	vm.swappiness = 1
	transparent_hugepage=never
	
	
	$ numactl --interleave=all mongod <options here>
	sudo numastat -p $(pidof mongod)
	$ sudo blockdev --getra /dev/sda
	
	mount: discard,noatime,barrier
	
	


# Usage


```

	StorageManager smgr = ...;

	// get the relation.
	rel = smgr.object(0x1).fork(PageFork.Main);

	// initialize a new page
	PageRef.init(npage);

	// extend the relation.
	int pageno = rel.extend(empty, npage);

	// read the new page through buffer manager.
	buffers.read(rel, pageno, page -> ...);

```

## Scanning
 
A storage object can also be scanned using a separate memory buffer pool, which means that pages won't be evicted from the main work buffer pool:
 
```
 
	rel.scan(bufpool).forEach(page -> ...);

```

# Multiple Pools

## Page Sizes

A buffer pool can only work with objects that have the same buffer size.  Multiple pools can be created for different object page sizes.

## Generational

Taking a leaf from the JVM GC book, pages which persist for some longer period of time or are continually loaded and evicted due to pressure can be loaded into a generational pool.  This pool is useful for pages containing lookup data that is regularly used and not just temporarily used.

Some pages have temporal locality - they are useful to pin around for some short period of time, as they are very likely to be needed again soon.  For example, a newly added page to a stream. 


# Temporary Buffers
 
A temporary buffer pool can be created for backing of pages which are ephemeral but can grow to an unbounded size.  These use work memory until there is pressure, at which point they are disk backed.  No WAL logging or durability is provided for the work buffers.


# Limits

- Pages: 2^31-1 (2,147,483,647)
- Page Size: min=512, max=65536
- Segments: 65,535

# Freespace Maps

- See [https://github.com/postgres/postgres/blob/master/src/backend/storage/freespace/README]

Pages contain tuples, which can only be as large as the page size minus the fixed header (22 bytes).  When using pages for data such as indexes or heap records that are not ordered by tuple entry and have entries added and removed during normal operation, it is vital to be able to quickly find some free space without having to scan all pages.  Transaction rollbacks also free up space when expired tuples are removed.

The freespace map kicks in after a certain number of pages (currently 2).

# Visibility Map

Because tuples may be inserted into objects which are not yet visible to all transactions.  If the transaction which inserted a tuple is rolled back, the vacuum process removes the expired tuples.

Rather than scan each page every time a vacuum is needed, this map contains a single bit per page.  The bit is set when a vacuum has completed on that page, and is cleared when the page is modified.

The visibility map only kicks in after a certain number of pages (currently 2).

# Sub Objects

An event sourcing system can have a large number of very small event streams that are rarely touched.  It is not ideal to map each stream to a file on the filesystem.  However, we want to easily access the whole set of tuples without seeking all over the place.

GDS supports multiplexed objects using a virtual lookup table.  These are objects which use a single object, but a virtual lookup table maps the inner objects to pages.  Each sub-object contains a list of pages for that object.

This allows smaller streams to start in an object with a small blocksize (e.g, 512), but progress to a larger one as it contains more records.  There is a small overhead hit as it is promoted to its own object, but the idea is this only happens rarely.

Stream hinting can advise GDS of the type of stream and preallocation size when it is being created, so it is allocated it own object immediately, without being promoted.

Virtual objects have an object ID that has the top 4 bits set to 0xFF.  The next 28 bits are the container object ID.  The last 32 bits are the virtual object identifier.

	[ Virtual Flag: 0xFF ] [ Container Object: 0xAA 0xAA 0xAA ] [ Virtual Object: 0xAA 0xAA 0xAA 0xAA ]

A lookup map keeps track of the virtual page for each object.  The map consists of an array of 32 bit integers, each one mapping to the page number for the virtual object.







# Maintenance

The WAL is a great source of information about modified pages, so can be used to perform maintenance work in a background thread on demand by reading the WAL journal.





# Testing / Benchmarking

gcloud compute --project "xxxxxxx" instances create "benchmark-1" --zone "us-central1-f" --machine-type "n1-highcpu-16" --subnet "default" --maintenance-policy "MIGRATE" --service-account "xxxxxx-compute@developer.gserviceaccount.com" --scopes "https://www.googleapis.com/auth/devstorage.read_only","https://www.googleapis.com/auth/logging.write","https://www.googleapis.com/auth/monitoring.write","https://www.googleapis.com/auth/servicecontrol","https://www.googleapis.com/auth/service.management.readonly","https://www.googleapis.com/auth/trace.append" --local-ssd interface="NVME" --local-ssd interface="NVME" --local-ssd interface="NVME" --local-ssd interface="NVME" --local-ssd interface="NVME" --local-ssd interface="NVME" --local-ssd interface="NVME" --local-ssd interface="NVME" --image "ubuntu-1704-zesty-v20170413" --image-project "ubuntu-os-cloud" --boot-disk-size "10" --boot-disk-type "pd-standard" --boot-disk-device-name "benchmark-1"





