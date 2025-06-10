package nbc.chillguys.nebulazone.domain.chat.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum MessageType {
	TEXT, IMAGE;

	@JsonCreator
    public static MessageType from(String value) {
        for (MessageType t : values()) {
            if (t.name().equalsIgnoreCase(value)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown MessageType: " + value);
    }
}
