// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: journal.proto

package io.ewok.gds.journal.messages;

public interface XLogEntryOrBuilder extends
    // @@protoc_insertion_point(interface_extends:gds.wal.XLogEntry)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.gds.wal.XLogEntry.Checkpoint checkpoint = 1;</code>
   */
  io.ewok.gds.journal.messages.XLogEntry.Checkpoint getCheckpoint();
  /**
   * <code>.gds.wal.XLogEntry.Checkpoint checkpoint = 1;</code>
   */
  io.ewok.gds.journal.messages.XLogEntry.CheckpointOrBuilder getCheckpointOrBuilder();

  public io.ewok.gds.journal.messages.XLogEntry.PayloadCase getPayloadCase();
}
