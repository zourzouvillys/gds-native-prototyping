/**
 * Binary object storage backed by GDS storage engine.
 *
 * Blob storage is used for larger records which don't fit inside a single page.
 * When such a record is inserted, a reference to the blob is stored instead,
 * and the actual content placed in a blob entry.
 *
 * Blob records are stored as a chain of pages. The header contains a list of
 * page numbers and offsets, as well as any other metadata.
 *
 * @author theo
 *
 */
package io.ewok.gds.blob;