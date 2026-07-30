package org.nextoracle.webauthn;

import com.yubico.webauthn.data.ByteArray;
import lombok.experimental.UtilityClass;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Helpers to convert between a {@link UUID} user id and the 16-byte WebAuthn
 * user handle ({@link ByteArray}).
 */
@UtilityClass
public class WebAuthnIds {

    public static ByteArray toUserHandle(UUID userId) {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(userId.getMostSignificantBits());
        buffer.putLong(userId.getLeastSignificantBits());
        return new ByteArray(buffer.array());
    }

    public static UUID toUserId(ByteArray userHandle) {
        ByteBuffer buffer = ByteBuffer.wrap(userHandle.getBytes());
        long high = buffer.getLong();
        long low = buffer.getLong();
        return new UUID(high, low);
    }
}

