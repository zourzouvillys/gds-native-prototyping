// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: journal.proto

package io.ewok.gds.journal.messages;

public interface JournalRecordOrBuilder extends
    // @@protoc_insertion_point(interface_extends:gds.wal.JournalRecord)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>repeated .gds.wal.JournalEntry entry = 1;</code>
   */
  java.util.List<io.ewok.gds.journal.messages.JournalEntry> 
      getEntryList();
  /**
   * <code>repeated .gds.wal.JournalEntry entry = 1;</code>
   */
  io.ewok.gds.journal.messages.JournalEntry getEntry(int index);
  /**
   * <code>repeated .gds.wal.JournalEntry entry = 1;</code>
   */
  int getEntryCount();
  /**
   * <code>repeated .gds.wal.JournalEntry entry = 1;</code>
   */
  java.util.List<? extends io.ewok.gds.journal.messages.JournalEntryOrBuilder> 
      getEntryOrBuilderList();
  /**
   * <code>repeated .gds.wal.JournalEntry entry = 1;</code>
   */
  io.ewok.gds.journal.messages.JournalEntryOrBuilder getEntryOrBuilder(
      int index);
}
