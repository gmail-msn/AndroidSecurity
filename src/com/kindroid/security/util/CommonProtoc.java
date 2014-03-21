// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: CommonProtoc.proto

package com.kindroid.security.util;

public final class CommonProtoc {
  private CommonProtoc() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  public enum RequestType
      implements com.google.protobuf.ProtocolMessageEnum {
    READ(0, 1),
    EDIT(1, 2),
    REGISTER(2, 3),
    ;
    
    
    public final int getNumber() { return value; }
    
    public static RequestType valueOf(int value) {
      switch (value) {
        case 1: return READ;
        case 2: return EDIT;
        case 3: return REGISTER;
        default: return null;
      }
    }
    
    public static com.google.protobuf.Internal.EnumLiteMap<RequestType>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static com.google.protobuf.Internal.EnumLiteMap<RequestType>
        internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<RequestType>() {
            public RequestType findValueByNumber(int number) {
              return RequestType.valueOf(number)
    ;        }
          };
    
    public final com.google.protobuf.Descriptors.EnumValueDescriptor
        getValueDescriptor() {
      return getDescriptor().getValues().get(index);
    }
    public final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptorForType() {
      return getDescriptor();
    }
    public static final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptor() {
      return com.kindroid.security.util.CommonProtoc.getDescriptor().getEnumTypes().get(0);
    }
    
    private static final RequestType[] VALUES = {
      READ, EDIT, REGISTER, 
    };
    public static RequestType valueOf(
        com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
      if (desc.getType() != getDescriptor()) {
        throw new java.lang.IllegalArgumentException(
          "EnumValueDescriptor is not for this type.");
      }
      return VALUES[desc.getIndex()];
    }
    private final int index;
    private final int value;
    private RequestType(int index, int value) {
      this.index = index;
      this.value = value;
    }
    
    static {
      com.kindroid.security.util.CommonProtoc.getDescriptor();
    }
    
    // @@protoc_insertion_point(enum_scope:com.kindroid.security.util.RequestType)
  }
  
  public enum EmailType
      implements com.google.protobuf.ProtocolMessageEnum {
    OTHER_EMAIL(0, 1),
    TUTORIAL(1, 2),
    ;
    
    
    public final int getNumber() { return value; }
    
    public static EmailType valueOf(int value) {
      switch (value) {
        case 1: return OTHER_EMAIL;
        case 2: return TUTORIAL;
        default: return null;
      }
    }
    
    public static com.google.protobuf.Internal.EnumLiteMap<EmailType>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static com.google.protobuf.Internal.EnumLiteMap<EmailType>
        internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<EmailType>() {
            public EmailType findValueByNumber(int number) {
              return EmailType.valueOf(number)
    ;        }
          };
    
    public final com.google.protobuf.Descriptors.EnumValueDescriptor
        getValueDescriptor() {
      return getDescriptor().getValues().get(index);
    }
    public final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptorForType() {
      return getDescriptor();
    }
    public static final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptor() {
      return com.kindroid.security.util.CommonProtoc.getDescriptor().getEnumTypes().get(1);
    }
    
    private static final EmailType[] VALUES = {
      OTHER_EMAIL, TUTORIAL, 
    };
    public static EmailType valueOf(
        com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
      if (desc.getType() != getDescriptor()) {
        throw new java.lang.IllegalArgumentException(
          "EnumValueDescriptor is not for this type.");
      }
      return VALUES[desc.getIndex()];
    }
    private final int index;
    private final int value;
    private EmailType(int index, int value) {
      this.index = index;
      this.value = value;
    }
    
    static {
      com.kindroid.security.util.CommonProtoc.getDescriptor();
    }
    
    // @@protoc_insertion_point(enum_scope:com.kindroid.security.util.EmailType)
  }
  
  public enum FeedbackType
      implements com.google.protobuf.ProtocolMessageEnum {
    GENERAL(0, 1),
    VIRUS(1, 2),
    NET(2, 3),
    FIREWALL(3, 4),
    TASK(4, 5),
    APP(5, 6),
    WATCHDOG(6, 7),
    BACKUP(7, 8),
    ACCOUNT(8, 9),
    CLIENT(9, 10),
    UPGRADE(10, 11),
    OTHER_FEEDBACK(11, 12),
    MESSAGE_INTERCEPT(12, 13),
    BOOT_SPEEDUP(13, 14),
    CACHE_CLEAN(14, 15),
    EXAMINE(15, 16),
    ;
    
    
    public final int getNumber() { return value; }
    
    public static FeedbackType valueOf(int value) {
      switch (value) {
        case 1: return GENERAL;
        case 2: return VIRUS;
        case 3: return NET;
        case 4: return FIREWALL;
        case 5: return TASK;
        case 6: return APP;
        case 7: return WATCHDOG;
        case 8: return BACKUP;
        case 9: return ACCOUNT;
        case 10: return CLIENT;
        case 11: return UPGRADE;
        case 12: return OTHER_FEEDBACK;
        case 13: return MESSAGE_INTERCEPT;
        case 14: return BOOT_SPEEDUP;
        case 15: return CACHE_CLEAN;
        case 16: return EXAMINE;
        default: return null;
      }
    }
    
    public static com.google.protobuf.Internal.EnumLiteMap<FeedbackType>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static com.google.protobuf.Internal.EnumLiteMap<FeedbackType>
        internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<FeedbackType>() {
            public FeedbackType findValueByNumber(int number) {
              return FeedbackType.valueOf(number)
    ;        }
          };
    
    public final com.google.protobuf.Descriptors.EnumValueDescriptor
        getValueDescriptor() {
      return getDescriptor().getValues().get(index);
    }
    public final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptorForType() {
      return getDescriptor();
    }
    public static final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptor() {
      return com.kindroid.security.util.CommonProtoc.getDescriptor().getEnumTypes().get(2);
    }
    
    private static final FeedbackType[] VALUES = {
      GENERAL, VIRUS, NET, FIREWALL, TASK, APP, WATCHDOG, BACKUP, ACCOUNT, CLIENT, UPGRADE, OTHER_FEEDBACK, MESSAGE_INTERCEPT, BOOT_SPEEDUP, CACHE_CLEAN, EXAMINE, 
    };
    public static FeedbackType valueOf(
        com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
      if (desc.getType() != getDescriptor()) {
        throw new java.lang.IllegalArgumentException(
          "EnumValueDescriptor is not for this type.");
      }
      return VALUES[desc.getIndex()];
    }
    private final int index;
    private final int value;
    private FeedbackType(int index, int value) {
      this.index = index;
      this.value = value;
    }
    
    static {
      com.kindroid.security.util.CommonProtoc.getDescriptor();
    }
    
    // @@protoc_insertion_point(enum_scope:com.kindroid.security.util.FeedbackType)
  }
  
  public enum ViewType
      implements com.google.protobuf.ProtocolMessageEnum {
    ALL_APP(0, 1),
    FREE_APP(1, 2),
    PAID_APP(2, 3),
    ;
    
    
    public final int getNumber() { return value; }
    
    public static ViewType valueOf(int value) {
      switch (value) {
        case 1: return ALL_APP;
        case 2: return FREE_APP;
        case 3: return PAID_APP;
        default: return null;
      }
    }
    
    public static com.google.protobuf.Internal.EnumLiteMap<ViewType>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static com.google.protobuf.Internal.EnumLiteMap<ViewType>
        internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<ViewType>() {
            public ViewType findValueByNumber(int number) {
              return ViewType.valueOf(number)
    ;        }
          };
    
    public final com.google.protobuf.Descriptors.EnumValueDescriptor
        getValueDescriptor() {
      return getDescriptor().getValues().get(index);
    }
    public final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptorForType() {
      return getDescriptor();
    }
    public static final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptor() {
      return com.kindroid.security.util.CommonProtoc.getDescriptor().getEnumTypes().get(3);
    }
    
    private static final ViewType[] VALUES = {
      ALL_APP, FREE_APP, PAID_APP, 
    };
    public static ViewType valueOf(
        com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
      if (desc.getType() != getDescriptor()) {
        throw new java.lang.IllegalArgumentException(
          "EnumValueDescriptor is not for this type.");
      }
      return VALUES[desc.getIndex()];
    }
    private final int index;
    private final int value;
    private ViewType(int index, int value) {
      this.index = index;
      this.value = value;
    }
    
    static {
      com.kindroid.security.util.CommonProtoc.getDescriptor();
    }
    
    // @@protoc_insertion_point(enum_scope:com.kindroid.security.util.ViewType)
  }
  
  public enum BannerType
      implements com.google.protobuf.ProtocolMessageEnum {
    PROMOTION_BANNER(0, 1),
    TOPIC_BANNER(1, 2),
    ;
    
    
    public final int getNumber() { return value; }
    
    public static BannerType valueOf(int value) {
      switch (value) {
        case 1: return PROMOTION_BANNER;
        case 2: return TOPIC_BANNER;
        default: return null;
      }
    }
    
    public static com.google.protobuf.Internal.EnumLiteMap<BannerType>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static com.google.protobuf.Internal.EnumLiteMap<BannerType>
        internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<BannerType>() {
            public BannerType findValueByNumber(int number) {
              return BannerType.valueOf(number)
    ;        }
          };
    
    public final com.google.protobuf.Descriptors.EnumValueDescriptor
        getValueDescriptor() {
      return getDescriptor().getValues().get(index);
    }
    public final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptorForType() {
      return getDescriptor();
    }
    public static final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptor() {
      return com.kindroid.security.util.CommonProtoc.getDescriptor().getEnumTypes().get(4);
    }
    
    private static final BannerType[] VALUES = {
      PROMOTION_BANNER, TOPIC_BANNER, 
    };
    public static BannerType valueOf(
        com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
      if (desc.getType() != getDescriptor()) {
        throw new java.lang.IllegalArgumentException(
          "EnumValueDescriptor is not for this type.");
      }
      return VALUES[desc.getIndex()];
    }
    private final int index;
    private final int value;
    private BannerType(int index, int value) {
      this.index = index;
      this.value = value;
    }
    
    static {
      com.kindroid.security.util.CommonProtoc.getDescriptor();
    }
    
    // @@protoc_insertion_point(enum_scope:com.kindroid.security.util.BannerType)
  }
  
  public enum TargetType
      implements com.google.protobuf.ProtocolMessageEnum {
    TARGET_APP(0, 1),
    TARGET_TOPIC(1, 2),
    ;
    
    
    public final int getNumber() { return value; }
    
    public static TargetType valueOf(int value) {
      switch (value) {
        case 1: return TARGET_APP;
        case 2: return TARGET_TOPIC;
        default: return null;
      }
    }
    
    public static com.google.protobuf.Internal.EnumLiteMap<TargetType>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static com.google.protobuf.Internal.EnumLiteMap<TargetType>
        internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<TargetType>() {
            public TargetType findValueByNumber(int number) {
              return TargetType.valueOf(number)
    ;        }
          };
    
    public final com.google.protobuf.Descriptors.EnumValueDescriptor
        getValueDescriptor() {
      return getDescriptor().getValues().get(index);
    }
    public final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptorForType() {
      return getDescriptor();
    }
    public static final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptor() {
      return com.kindroid.security.util.CommonProtoc.getDescriptor().getEnumTypes().get(5);
    }
    
    private static final TargetType[] VALUES = {
      TARGET_APP, TARGET_TOPIC, 
    };
    public static TargetType valueOf(
        com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
      if (desc.getType() != getDescriptor()) {
        throw new java.lang.IllegalArgumentException(
          "EnumValueDescriptor is not for this type.");
      }
      return VALUES[desc.getIndex()];
    }
    private final int index;
    private final int value;
    private TargetType(int index, int value) {
      this.index = index;
      this.value = value;
    }
    
    static {
      com.kindroid.security.util.CommonProtoc.getDescriptor();
    }
    
    // @@protoc_insertion_point(enum_scope:com.kindroid.security.util.TargetType)
  }
  
  public enum TopicType
      implements com.google.protobuf.ProtocolMessageEnum {
    TOPIC_APP(0, 1),
    TOPIC_CATEGORY(1, 2),
    ;
    
    
    public final int getNumber() { return value; }
    
    public static TopicType valueOf(int value) {
      switch (value) {
        case 1: return TOPIC_APP;
        case 2: return TOPIC_CATEGORY;
        default: return null;
      }
    }
    
    public static com.google.protobuf.Internal.EnumLiteMap<TopicType>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static com.google.protobuf.Internal.EnumLiteMap<TopicType>
        internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<TopicType>() {
            public TopicType findValueByNumber(int number) {
              return TopicType.valueOf(number)
    ;        }
          };
    
    public final com.google.protobuf.Descriptors.EnumValueDescriptor
        getValueDescriptor() {
      return getDescriptor().getValues().get(index);
    }
    public final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptorForType() {
      return getDescriptor();
    }
    public static final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptor() {
      return com.kindroid.security.util.CommonProtoc.getDescriptor().getEnumTypes().get(6);
    }
    
    private static final TopicType[] VALUES = {
      TOPIC_APP, TOPIC_CATEGORY, 
    };
    public static TopicType valueOf(
        com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
      if (desc.getType() != getDescriptor()) {
        throw new java.lang.IllegalArgumentException(
          "EnumValueDescriptor is not for this type.");
      }
      return VALUES[desc.getIndex()];
    }
    private final int index;
    private final int value;
    private TopicType(int index, int value) {
      this.index = index;
      this.value = value;
    }
    
    static {
      com.kindroid.security.util.CommonProtoc.getDescriptor();
    }
    
    // @@protoc_insertion_point(enum_scope:com.kindroid.security.util.TopicType)
  }
  
  public enum ResultType
      implements com.google.protobuf.ProtocolMessageEnum {
    RESULT_APP(0, 1),
    RESULT_CATEGORY(1, 2),
    ;
    
    
    public final int getNumber() { return value; }
    
    public static ResultType valueOf(int value) {
      switch (value) {
        case 1: return RESULT_APP;
        case 2: return RESULT_CATEGORY;
        default: return null;
      }
    }
    
    public static com.google.protobuf.Internal.EnumLiteMap<ResultType>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static com.google.protobuf.Internal.EnumLiteMap<ResultType>
        internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<ResultType>() {
            public ResultType findValueByNumber(int number) {
              return ResultType.valueOf(number)
    ;        }
          };
    
    public final com.google.protobuf.Descriptors.EnumValueDescriptor
        getValueDescriptor() {
      return getDescriptor().getValues().get(index);
    }
    public final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptorForType() {
      return getDescriptor();
    }
    public static final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptor() {
      return com.kindroid.security.util.CommonProtoc.getDescriptor().getEnumTypes().get(7);
    }
    
    private static final ResultType[] VALUES = {
      RESULT_APP, RESULT_CATEGORY, 
    };
    public static ResultType valueOf(
        com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
      if (desc.getType() != getDescriptor()) {
        throw new java.lang.IllegalArgumentException(
          "EnumValueDescriptor is not for this type.");
      }
      return VALUES[desc.getIndex()];
    }
    private final int index;
    private final int value;
    private ResultType(int index, int value) {
      this.index = index;
      this.value = value;
    }
    
    static {
      com.kindroid.security.util.CommonProtoc.getDescriptor();
    }
    
    // @@protoc_insertion_point(enum_scope:com.kindroid.security.util.ResultType)
  }
  
  
  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\022CommonProtoc.proto\022\032com.kindroid.secur" +
      "ity.util*/\n\013RequestType\022\010\n\004READ\020\001\022\010\n\004EDI" +
      "T\020\002\022\014\n\010REGISTER\020\003**\n\tEmailType\022\017\n\013OTHER_" +
      "EMAIL\020\001\022\014\n\010TUTORIAL\020\002*\353\001\n\014FeedbackType\022\013" +
      "\n\007GENERAL\020\001\022\t\n\005VIRUS\020\002\022\007\n\003NET\020\003\022\014\n\010FIREW" +
      "ALL\020\004\022\010\n\004TASK\020\005\022\007\n\003APP\020\006\022\014\n\010WATCHDOG\020\007\022\n" +
      "\n\006BACKUP\020\010\022\013\n\007ACCOUNT\020\t\022\n\n\006CLIENT\020\n\022\013\n\007U" +
      "PGRADE\020\013\022\022\n\016OTHER_FEEDBACK\020\014\022\025\n\021MESSAGE_" +
      "INTERCEPT\020\r\022\020\n\014BOOT_SPEEDUP\020\016\022\017\n\013CACHE_C" +
      "LEAN\020\017\022\013\n\007EXAMINE\020\020*3\n\010ViewType\022\013\n\007ALL_A",
      "PP\020\001\022\014\n\010FREE_APP\020\002\022\014\n\010PAID_APP\020\003*4\n\nBann" +
      "erType\022\024\n\020PROMOTION_BANNER\020\001\022\020\n\014TOPIC_BA" +
      "NNER\020\002*.\n\nTargetType\022\016\n\nTARGET_APP\020\001\022\020\n\014" +
      "TARGET_TOPIC\020\002*.\n\tTopicType\022\r\n\tTOPIC_APP" +
      "\020\001\022\022\n\016TOPIC_CATEGORY\020\002*1\n\nResultType\022\016\n\n" +
      "RESULT_APP\020\001\022\023\n\017RESULT_CATEGORY\020\002B\034\n\032com" +
      ".kindroid.security.util"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
      new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
        public com.google.protobuf.ExtensionRegistry assignDescriptors(
            com.google.protobuf.Descriptors.FileDescriptor root) {
          descriptor = root;
          return null;
        }
      };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
  }
  
  public static void internalForceInit() {}
  
  // @@protoc_insertion_point(outer_class_scope)
}