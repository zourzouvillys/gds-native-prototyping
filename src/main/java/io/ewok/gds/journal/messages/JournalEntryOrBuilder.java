// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: journal.proto

package io.ewok.gds.journal.messages;

public interface JournalEntryOrBuilder extends
    // @@protoc_insertion_point(interface_extends:gds.wal.JournalEntry)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.gds.wal.XLogEntry xlog = 1;</code>
   */
  io.ewok.gds.journal.messages.XLogEntry getXlog();
  /**
   * <code>.gds.wal.XLogEntry xlog = 1;</code>
   */
  io.ewok.gds.journal.messages.XLogEntryOrBuilder getXlogOrBuilder();

  /**
   * <code>.gds.wal.TxnAcctEntry txn_acct = 2;</code>
   */
  io.ewok.gds.journal.messages.TxnAcctEntry getTxnAcct();
  /**
   * <code>.gds.wal.TxnAcctEntry txn_acct = 2;</code>
   */
  io.ewok.gds.journal.messages.TxnAcctEntryOrBuilder getTxnAcctOrBuilder();

  /**
   * <code>.gds.wal.StorageEntry storage = 3;</code>
   */
  io.ewok.gds.journal.messages.StorageEntry getStorage();
  /**
   * <code>.gds.wal.StorageEntry storage = 3;</code>
   */
  io.ewok.gds.journal.messages.StorageEntryOrBuilder getStorageOrBuilder();

  /**
   * <code>.gds.wal.HeapEntry heap = 4;</code>
   */
  io.ewok.gds.journal.messages.HeapEntry getHeap();
  /**
   * <code>.gds.wal.HeapEntry heap = 4;</code>
   */
  io.ewok.gds.journal.messages.HeapEntryOrBuilder getHeapOrBuilder();

  /**
   * <code>.gds.wal.IndexEntry index = 5;</code>
   */
  io.ewok.gds.journal.messages.IndexEntry getIndex();
  /**
   * <code>.gds.wal.IndexEntry index = 5;</code>
   */
  io.ewok.gds.journal.messages.IndexEntryOrBuilder getIndexOrBuilder();

  /**
   * <code>.gds.wal.CheckpointEntry checkpoint = 6;</code>
   */
  io.ewok.gds.journal.messages.CheckpointEntry getCheckpoint();
  /**
   * <code>.gds.wal.CheckpointEntry checkpoint = 6;</code>
   */
  io.ewok.gds.journal.messages.CheckpointEntryOrBuilder getCheckpointOrBuilder();

  /**
   * <code>.gds.wal.BlobEntry blob = 7;</code>
   */
  io.ewok.gds.journal.messages.BlobEntry getBlob();
  /**
   * <code>.gds.wal.BlobEntry blob = 7;</code>
   */
  io.ewok.gds.journal.messages.BlobEntryOrBuilder getBlobOrBuilder();

  public io.ewok.gds.journal.messages.JournalEntry.ResourceCase getResourceCase();
}
