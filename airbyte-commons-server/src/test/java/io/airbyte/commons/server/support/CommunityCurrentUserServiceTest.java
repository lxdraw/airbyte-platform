/*
 * Copyright (c) 2020-2024 Airbyte, Inc., all rights reserved.
 */

package io.airbyte.commons.server.support;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.airbyte.config.User;
import io.airbyte.config.persistence.UserPersistence;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.context.ServerRequestContext;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@MicronautTest
@Requires(env = {Environment.TEST})
@Property(name = "micronaut.security.enabled",
          value = "false")
public class CommunityCurrentUserServiceTest {

  @MockBean(UserPersistence.class)
  UserPersistence mockUserPersistence() {
    return Mockito.mock(UserPersistence.class);
  }

  @Inject
  CommunityCurrentUserService currentUserService;

  @Inject
  UserPersistence userPersistence;

  @BeforeEach
  void setUp() {
    // set up a mock request context, details don't matter, just needed to make the
    // @RequestScope work on the CommunityCurrentUserService
    ServerRequestContext.set(HttpRequest.GET("/"));
  }

  @Test
  void testGetCurrentUser() throws IOException {
    final User expectedUser = new User().withUserId(UserPersistence.DEFAULT_USER_ID);
    when(userPersistence.getDefaultUser()).thenReturn(Optional.ofNullable(expectedUser));

    // First call - should fetch default user from userPersistence
    final User user1 = currentUserService.getCurrentUser();
    Assertions.assertEquals(expectedUser, user1);

    // Second call - should use cached user
    final User user2 = currentUserService.getCurrentUser();
    Assertions.assertEquals(expectedUser, user2);

    // Verify that getDefaultUser is called only once
    verify(userPersistence, times(1)).getDefaultUser();
  }

}
