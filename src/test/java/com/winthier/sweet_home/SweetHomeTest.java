package com.winthier.sweet_home;

import java.util.Map;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;

public final class SweetHomeTest {
    @Test
    public void main() {
        // Test home (de)serialization
        {
            Home home = new Home();
            home.setOwner(UUID.randomUUID());
            home.setName("foo");
            home.setWorld("world");
            home.setX(1);
            home.setY(2);
            home.setZ(3);
            home.setPitch(4);
            home.setYaw(5);
            home.getInvites().add(UUID.randomUUID());
            home.getInvites().add(UUID.randomUUID());
            home.getInvites().add(UUID.randomUUID());
            home.getInvites().add(UUID.randomUUID());
            home.setCreationTime(System.currentTimeMillis());
            home.setDescription("Test Home");
            Map<?, ?> serialized = home.serialize();
            Home home2 = new Home(serialized);
            Map<?, ?> serialized2 = home2.serialize();
            Assert.assertEquals(home, home2);
            Assert.assertEquals(serialized, serialized2);
        }
        // Test User (de)serialization
        {
            User user = new User(UUID.randomUUID());
            user.setExtraHomes(3);
            Map<?, ?> serialized = user.serialize();
            User user2 = new User(serialized);
            Map<?, ?> serialized2 = user2.serialize();
            Assert.assertEquals(user, user2);
            Assert.assertEquals(serialized, serialized2);
        }
    }
}
