// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: journal.proto

package io.ewok.gds.journal.messages;

public interface TxnAcctEntryOrBuilder extends
    // @@protoc_insertion_point(interface_extends:gds.wal.TxnAcctEntry)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.gds.wal.TxnBegin txn_begin = 1;</code>
   */
  io.ewok.gds.journal.messages.TxnBegin getTxnBegin();
  /**
   * <code>.gds.wal.TxnBegin txn_begin = 1;</code>
   */
  io.ewok.gds.journal.messages.TxnBeginOrBuilder getTxnBeginOrBuilder();

  /**
   * <code>.gds.wal.TxnCommit txn_commit = 2;</code>
   */
  io.ewok.gds.journal.messages.TxnCommit getTxnCommit();
  /**
   * <code>.gds.wal.TxnCommit txn_commit = 2;</code>
   */
  io.ewok.gds.journal.messages.TxnCommitOrBuilder getTxnCommitOrBuilder();

  /**
   * <code>.gds.wal.TxnAbort txn_abort = 3;</code>
   */
  io.ewok.gds.journal.messages.TxnAbort getTxnAbort();
  /**
   * <code>.gds.wal.TxnAbort txn_abort = 3;</code>
   */
  io.ewok.gds.journal.messages.TxnAbortOrBuilder getTxnAbortOrBuilder();

  public io.ewok.gds.journal.messages.TxnAcctEntry.PayloadCase getPayloadCase();
}
