package com.smoketurner.notification.application.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import io.dropwizard.jersey.errors.ErrorMessage;
import io.dropwizard.testing.junit.ResourceTestRule;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.smoketurner.notification.api.Notification;
import com.smoketurner.notification.application.exceptions.NotificationExceptionMapper;
import com.smoketurner.notification.application.exceptions.NotificationStoreException;
import com.smoketurner.notification.application.filter.CharsetResponseFilter;
import com.smoketurner.notification.application.store.NotificationStore;

public class NotificationResourceTest {

    private static final NotificationStore store = mock(NotificationStore.class);

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new NotificationResource(store))
            .addProvider(new CharsetResponseFilter())
            .addProvider(new NotificationExceptionMapper()).build();

    @Before
    public void setUp() {
        reset(store);
    }

    @Test
    public void testFetch() throws Exception {
        final List<Notification> expected = ImmutableList.of(Notification
                .builder().withId(1L).build());
        when(store.fetch("test")).thenReturn(Optional.of(expected));

        final Response response = resources.client()
                .target("/v1/notifications/test")
                .request(MediaType.APPLICATION_JSON).get();
        final List<Notification> actual = response
                .readEntity(new GenericType<List<Notification>>() {
                });

        verify(store).fetch("test");
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getHeaderString(HttpHeaders.CONTENT_TYPE))
                .isEqualTo(MediaType.APPLICATION_JSON + ";charset=UTF-8");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testFetchNotFound() throws Exception {
        when(store.fetch("test")).thenReturn(
                Optional.<List<Notification>> absent());

        final Response response = resources.client()
                .target("/v1/notifications/test")
                .request(MediaType.APPLICATION_JSON).get();
        final ErrorMessage actual = response.readEntity(ErrorMessage.class);

        verify(store).fetch("test");
        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(actual.getCode()).isEqualTo(404);
    }

    @Test
    public void testFetchException() throws Exception {
        when(store.fetch("test")).thenThrow(new NotificationStoreException());

        final Response response = resources.client()
                .target("/v1/notifications/test")
                .request(MediaType.APPLICATION_JSON).get();
        final ErrorMessage actual = response.readEntity(ErrorMessage.class);

        verify(store).fetch("test");
        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(actual.getCode()).isEqualTo(500);
    }

    @Test
    public void testStore() throws Exception {
        final Notification expected = Notification.builder().withId(1L)
                .withCategory("test-category").withMessage("testing 1 2 3")
                .withCreatedAt(DateTime.now(DateTimeZone.UTC)).build();

        final Notification notification = Notification.builder()
                .withCategory("test-category").withMessage("testing 1 2 3")
                .build();

        when(store.store("test", notification)).thenReturn(expected);

        final Response response = resources.client()
                .target("/v1/notifications/test")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(notification));
        final Notification actual = response.readEntity(Notification.class);

        verify(store).store("test", notification);
        assertThat(response.getStatus()).isEqualTo(201);
        assertThat(response.getLocation().getPath()).isEqualTo(
                "/v1/notifications/test");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testStoreException() throws Exception {
        final Notification notification = Notification.builder()
                .withCategory("test-category").withMessage("testing 1 2 3")
                .build();
        when(store.store("test", notification)).thenThrow(
                new NotificationStoreException());

        final Response response = resources.client()
                .target("/v1/notifications/test")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(notification));
        final ErrorMessage actual = response.readEntity(ErrorMessage.class);

        verify(store).store("test", notification);
        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(actual.getCode()).isEqualTo(500);
    }

    @Test
    public void testStoreMissingCategory() throws Exception {
        final Notification notification = Notification.builder()
                .withMessage("testing 1 2 3").build();

        try {
            resources.client().target("/v1/notifications/test")
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(notification));
        } catch (ProcessingException e) {
            assertThat(e.getCause()).isInstanceOf(
                    ConstraintViolationException.class);
        }

        verify(store, never()).store("test", notification);
    }

    @Test
    public void testStoreEmptyCategory() throws Exception {
        final Notification notification = Notification.builder()
                .withCategory("").withMessage("testing 1 2 3").build();

        try {
            resources.client().target("/v1/notifications/test")
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(notification));
        } catch (ProcessingException e) {
            assertThat(e.getCause()).isInstanceOf(
                    ConstraintViolationException.class);
        }

        verify(store, never()).store("test", notification);
    }

    @Test
    public void testStoreMissingMessage() throws Exception {
        final Notification notification = Notification.builder()
                .withCategory("test-category").build();

        try {
            resources.client().target("/v1/notifications/test")
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(notification));
        } catch (ProcessingException e) {
            assertThat(e.getCause()).isInstanceOf(
                    ConstraintViolationException.class);
        }

        verify(store, never()).store("test", notification);
    }

    @Test
    public void testStoreEmptyMessage() throws Exception {
        final Notification notification = Notification.builder()
                .withCategory("test-category").withMessage("").build();

        try {
            resources.client().target("/v1/notifications/test")
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(notification));
        } catch (ProcessingException e) {
            assertThat(e.getCause()).isInstanceOf(
                    ConstraintViolationException.class);
        }

        verify(store, never()).store("test", notification);
    }

    @Test
    public void testRemove() throws Exception {
        final Response response = resources.client()
                .target("/v1/notifications/test").request().delete();

        verify(store).removeAll("test");
        assertThat(response.getStatus()).isEqualTo(204);
    }

    @Test
    public void testRemoveException() throws Exception {
        doThrow(new NotificationStoreException()).when(store).removeAll("test");
        final Response response = resources.client()
                .target("/v1/notifications/test").request().delete();
        final ErrorMessage actual = response.readEntity(ErrorMessage.class);

        verify(store).removeAll("test");
        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(actual.getCode()).isEqualTo(500);
    }

    @Test
    public void testRemoveIds() throws Exception {
        final Response response = resources.client()
                .target("/v1/notifications/test?ids=1,2,asdf,3").request()
                .delete();

        verify(store).remove("test", ImmutableSet.of(1L, 2L, 3L));
        verify(store, never()).removeAll(anyString());
        assertThat(response.getStatus()).isEqualTo(204);
    }

    @Test
    public void testRemoveIdsException() throws Exception {
        final Set<Long> ids = ImmutableSet.of(1L, 2L, 3L);
        doThrow(new NotificationStoreException()).when(store).remove("test",
                ids);
        final Response response = resources.client()
                .target("/v1/notifications/test?ids=1,2,asdf,3").request()
                .delete();
        final ErrorMessage actual = response.readEntity(ErrorMessage.class);

        verify(store).remove("test", ids);
        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(actual.getCode()).isEqualTo(500);
    }
}
