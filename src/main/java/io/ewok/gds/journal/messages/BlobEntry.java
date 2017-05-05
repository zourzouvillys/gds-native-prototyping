// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: journal.proto

package io.ewok.gds.journal.messages;

/**
 * Protobuf type {@code gds.wal.BlobEntry}
 */
public  final class BlobEntry extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:gds.wal.BlobEntry)
    BlobEntryOrBuilder {
  // Use BlobEntry.newBuilder() to construct.
  private BlobEntry(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private BlobEntry() {
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return com.google.protobuf.UnknownFieldSet.getDefaultInstance();
  }
  private BlobEntry(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    this();
    int mutable_bitField0_ = 0;
    try {
      boolean done = false;
      while (!done) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            done = true;
            break;
          default: {
            if (!input.skipField(tag)) {
              done = true;
            }
            break;
          }
          case 10: {
            io.ewok.gds.journal.messages.StorageCreate.Builder subBuilder = null;
            if (payloadCase_ == 1) {
              subBuilder = ((io.ewok.gds.journal.messages.StorageCreate) payload_).toBuilder();
            }
            payload_ =
                input.readMessage(io.ewok.gds.journal.messages.StorageCreate.parser(), extensionRegistry);
            if (subBuilder != null) {
              subBuilder.mergeFrom((io.ewok.gds.journal.messages.StorageCreate) payload_);
              payload_ = subBuilder.buildPartial();
            }
            payloadCase_ = 1;
            break;
          }
        }
      }
    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (java.io.IOException e) {
      throw new com.google.protobuf.InvalidProtocolBufferException(
          e).setUnfinishedMessage(this);
    } finally {
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.ewok.gds.journal.messages.JournalProto.internal_static_gds_wal_BlobEntry_descriptor;
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.ewok.gds.journal.messages.JournalProto.internal_static_gds_wal_BlobEntry_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.ewok.gds.journal.messages.BlobEntry.class, io.ewok.gds.journal.messages.BlobEntry.Builder.class);
  }

  private int payloadCase_ = 0;
  private java.lang.Object payload_;
  public enum PayloadCase
      implements com.google.protobuf.Internal.EnumLite {
    CREATE(1),
    PAYLOAD_NOT_SET(0);
    private final int value;
    private PayloadCase(int value) {
      this.value = value;
    }
    /**
     * @deprecated Use {@link #forNumber(int)} instead.
     */
    @java.lang.Deprecated
    public static PayloadCase valueOf(int value) {
      return forNumber(value);
    }

    public static PayloadCase forNumber(int value) {
      switch (value) {
        case 1: return CREATE;
        case 0: return PAYLOAD_NOT_SET;
        default: return null;
      }
    }
    public int getNumber() {
      return this.value;
    }
  };

  public PayloadCase
  getPayloadCase() {
    return PayloadCase.forNumber(
        payloadCase_);
  }

  public static final int CREATE_FIELD_NUMBER = 1;
  /**
   * <code>.gds.wal.StorageCreate create = 1;</code>
   */
  public io.ewok.gds.journal.messages.StorageCreate getCreate() {
    if (payloadCase_ == 1) {
       return (io.ewok.gds.journal.messages.StorageCreate) payload_;
    }
    return io.ewok.gds.journal.messages.StorageCreate.getDefaultInstance();
  }
  /**
   * <code>.gds.wal.StorageCreate create = 1;</code>
   */
  public io.ewok.gds.journal.messages.StorageCreateOrBuilder getCreateOrBuilder() {
    if (payloadCase_ == 1) {
       return (io.ewok.gds.journal.messages.StorageCreate) payload_;
    }
    return io.ewok.gds.journal.messages.StorageCreate.getDefaultInstance();
  }

  private byte memoizedIsInitialized = -1;
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (payloadCase_ == 1) {
      output.writeMessage(1, (io.ewok.gds.journal.messages.StorageCreate) payload_);
    }
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (payloadCase_ == 1) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, (io.ewok.gds.journal.messages.StorageCreate) payload_);
    }
    memoizedSize = size;
    return size;
  }

  private static final long serialVersionUID = 0L;
  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof io.ewok.gds.journal.messages.BlobEntry)) {
      return super.equals(obj);
    }
    io.ewok.gds.journal.messages.BlobEntry other = (io.ewok.gds.journal.messages.BlobEntry) obj;

    boolean result = true;
    result = result && getPayloadCase().equals(
        other.getPayloadCase());
    if (!result) return false;
    switch (payloadCase_) {
      case 1:
        result = result && getCreate()
            .equals(other.getCreate());
        break;
      case 0:
      default:
    }
    return result;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    switch (payloadCase_) {
      case 1:
        hash = (37 * hash) + CREATE_FIELD_NUMBER;
        hash = (53 * hash) + getCreate().hashCode();
        break;
      case 0:
      default:
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.ewok.gds.journal.messages.BlobEntry parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.ewok.gds.journal.messages.BlobEntry parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.ewok.gds.journal.messages.BlobEntry parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.ewok.gds.journal.messages.BlobEntry parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.ewok.gds.journal.messages.BlobEntry parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.ewok.gds.journal.messages.BlobEntry parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.ewok.gds.journal.messages.BlobEntry parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.ewok.gds.journal.messages.BlobEntry parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.ewok.gds.journal.messages.BlobEntry parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static io.ewok.gds.journal.messages.BlobEntry parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.ewok.gds.journal.messages.BlobEntry parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.ewok.gds.journal.messages.BlobEntry parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(io.ewok.gds.journal.messages.BlobEntry prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * Protobuf type {@code gds.wal.BlobEntry}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:gds.wal.BlobEntry)
      io.ewok.gds.journal.messages.BlobEntryOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.ewok.gds.journal.messages.JournalProto.internal_static_gds_wal_BlobEntry_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.ewok.gds.journal.messages.JournalProto.internal_static_gds_wal_BlobEntry_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.ewok.gds.journal.messages.BlobEntry.class, io.ewok.gds.journal.messages.BlobEntry.Builder.class);
    }

    // Construct using io.ewok.gds.journal.messages.BlobEntry.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessageV3
              .alwaysUseFieldBuilders) {
      }
    }
    public Builder clear() {
      super.clear();
      payloadCase_ = 0;
      payload_ = null;
      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.ewok.gds.journal.messages.JournalProto.internal_static_gds_wal_BlobEntry_descriptor;
    }

    public io.ewok.gds.journal.messages.BlobEntry getDefaultInstanceForType() {
      return io.ewok.gds.journal.messages.BlobEntry.getDefaultInstance();
    }

    public io.ewok.gds.journal.messages.BlobEntry build() {
      io.ewok.gds.journal.messages.BlobEntry result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public io.ewok.gds.journal.messages.BlobEntry buildPartial() {
      io.ewok.gds.journal.messages.BlobEntry result = new io.ewok.gds.journal.messages.BlobEntry(this);
      if (payloadCase_ == 1) {
        if (createBuilder_ == null) {
          result.payload_ = payload_;
        } else {
          result.payload_ = createBuilder_.build();
        }
      }
      result.payloadCase_ = payloadCase_;
      onBuilt();
      return result;
    }

    public Builder clone() {
      return (Builder) super.clone();
    }
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        Object value) {
      return (Builder) super.setField(field, value);
    }
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return (Builder) super.clearField(field);
    }
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return (Builder) super.clearOneof(oneof);
    }
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, Object value) {
      return (Builder) super.setRepeatedField(field, index, value);
    }
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        Object value) {
      return (Builder) super.addRepeatedField(field, value);
    }
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof io.ewok.gds.journal.messages.BlobEntry) {
        return mergeFrom((io.ewok.gds.journal.messages.BlobEntry)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.ewok.gds.journal.messages.BlobEntry other) {
      if (other == io.ewok.gds.journal.messages.BlobEntry.getDefaultInstance()) return this;
      switch (other.getPayloadCase()) {
        case CREATE: {
          mergeCreate(other.getCreate());
          break;
        }
        case PAYLOAD_NOT_SET: {
          break;
        }
      }
      onChanged();
      return this;
    }

    public final boolean isInitialized() {
      return true;
    }

    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      io.ewok.gds.journal.messages.BlobEntry parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (io.ewok.gds.journal.messages.BlobEntry) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }
    private int payloadCase_ = 0;
    private java.lang.Object payload_;
    public PayloadCase
        getPayloadCase() {
      return PayloadCase.forNumber(
          payloadCase_);
    }

    public Builder clearPayload() {
      payloadCase_ = 0;
      payload_ = null;
      onChanged();
      return this;
    }


    private com.google.protobuf.SingleFieldBuilderV3<
        io.ewok.gds.journal.messages.StorageCreate, io.ewok.gds.journal.messages.StorageCreate.Builder, io.ewok.gds.journal.messages.StorageCreateOrBuilder> createBuilder_;
    /**
     * <code>.gds.wal.StorageCreate create = 1;</code>
     */
    public io.ewok.gds.journal.messages.StorageCreate getCreate() {
      if (createBuilder_ == null) {
        if (payloadCase_ == 1) {
          return (io.ewok.gds.journal.messages.StorageCreate) payload_;
        }
        return io.ewok.gds.journal.messages.StorageCreate.getDefaultInstance();
      } else {
        if (payloadCase_ == 1) {
          return createBuilder_.getMessage();
        }
        return io.ewok.gds.journal.messages.StorageCreate.getDefaultInstance();
      }
    }
    /**
     * <code>.gds.wal.StorageCreate create = 1;</code>
     */
    public Builder setCreate(io.ewok.gds.journal.messages.StorageCreate value) {
      if (createBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        payload_ = value;
        onChanged();
      } else {
        createBuilder_.setMessage(value);
      }
      payloadCase_ = 1;
      return this;
    }
    /**
     * <code>.gds.wal.StorageCreate create = 1;</code>
     */
    public Builder setCreate(
        io.ewok.gds.journal.messages.StorageCreate.Builder builderForValue) {
      if (createBuilder_ == null) {
        payload_ = builderForValue.build();
        onChanged();
      } else {
        createBuilder_.setMessage(builderForValue.build());
      }
      payloadCase_ = 1;
      return this;
    }
    /**
     * <code>.gds.wal.StorageCreate create = 1;</code>
     */
    public Builder mergeCreate(io.ewok.gds.journal.messages.StorageCreate value) {
      if (createBuilder_ == null) {
        if (payloadCase_ == 1 &&
            payload_ != io.ewok.gds.journal.messages.StorageCreate.getDefaultInstance()) {
          payload_ = io.ewok.gds.journal.messages.StorageCreate.newBuilder((io.ewok.gds.journal.messages.StorageCreate) payload_)
              .mergeFrom(value).buildPartial();
        } else {
          payload_ = value;
        }
        onChanged();
      } else {
        if (payloadCase_ == 1) {
          createBuilder_.mergeFrom(value);
        }
        createBuilder_.setMessage(value);
      }
      payloadCase_ = 1;
      return this;
    }
    /**
     * <code>.gds.wal.StorageCreate create = 1;</code>
     */
    public Builder clearCreate() {
      if (createBuilder_ == null) {
        if (payloadCase_ == 1) {
          payloadCase_ = 0;
          payload_ = null;
          onChanged();
        }
      } else {
        if (payloadCase_ == 1) {
          payloadCase_ = 0;
          payload_ = null;
        }
        createBuilder_.clear();
      }
      return this;
    }
    /**
     * <code>.gds.wal.StorageCreate create = 1;</code>
     */
    public io.ewok.gds.journal.messages.StorageCreate.Builder getCreateBuilder() {
      return getCreateFieldBuilder().getBuilder();
    }
    /**
     * <code>.gds.wal.StorageCreate create = 1;</code>
     */
    public io.ewok.gds.journal.messages.StorageCreateOrBuilder getCreateOrBuilder() {
      if ((payloadCase_ == 1) && (createBuilder_ != null)) {
        return createBuilder_.getMessageOrBuilder();
      } else {
        if (payloadCase_ == 1) {
          return (io.ewok.gds.journal.messages.StorageCreate) payload_;
        }
        return io.ewok.gds.journal.messages.StorageCreate.getDefaultInstance();
      }
    }
    /**
     * <code>.gds.wal.StorageCreate create = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        io.ewok.gds.journal.messages.StorageCreate, io.ewok.gds.journal.messages.StorageCreate.Builder, io.ewok.gds.journal.messages.StorageCreateOrBuilder> 
        getCreateFieldBuilder() {
      if (createBuilder_ == null) {
        if (!(payloadCase_ == 1)) {
          payload_ = io.ewok.gds.journal.messages.StorageCreate.getDefaultInstance();
        }
        createBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            io.ewok.gds.journal.messages.StorageCreate, io.ewok.gds.journal.messages.StorageCreate.Builder, io.ewok.gds.journal.messages.StorageCreateOrBuilder>(
                (io.ewok.gds.journal.messages.StorageCreate) payload_,
                getParentForChildren(),
                isClean());
        payload_ = null;
      }
      payloadCase_ = 1;
      onChanged();;
      return createBuilder_;
    }
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return this;
    }

    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return this;
    }


    // @@protoc_insertion_point(builder_scope:gds.wal.BlobEntry)
  }

  // @@protoc_insertion_point(class_scope:gds.wal.BlobEntry)
  private static final io.ewok.gds.journal.messages.BlobEntry DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.ewok.gds.journal.messages.BlobEntry();
  }

  public static io.ewok.gds.journal.messages.BlobEntry getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<BlobEntry>
      PARSER = new com.google.protobuf.AbstractParser<BlobEntry>() {
    public BlobEntry parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
        return new BlobEntry(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<BlobEntry> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<BlobEntry> getParserForType() {
    return PARSER;
  }

  public io.ewok.gds.journal.messages.BlobEntry getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

