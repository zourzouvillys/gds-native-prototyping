# EWOK GDS Buffers

Low level buffer & page management.

The library consists of a low level page access service which performs transfers to from storage (e.g, disk), and a buffer manager which performs page caching, eviction, and a write ahead log service.

The API is exposed through io.ewok.gds.buffers.BufferManager.

## Consistency

When a page larger than the disk block size is written to disk, there is no guarantee of atomicity.  A partial page write can occur if the power fails while performing a page write.  This is known as a "fragmented block".

To combat this, GDS creates a copt of a page before writting to disk, and waits until it has been flushed before writing. Once the dirty page has been flushed, then the journal entry can be removed/reused.  The clean page may be copied using transferTo to improve performance, as the page does not need to be copied into userspace.

Another option is to use a filesystem which supports atomic writes.  In these cases, full page writes are guarunteed so there is no need to perform page copies.

If a consumer doesn't care about potentially broken page because it can retrieve it from somewhere else (e.g, a backup replica), then page backups are not needed.


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
	
	




 


