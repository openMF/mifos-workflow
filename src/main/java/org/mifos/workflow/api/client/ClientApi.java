package org.mifos.workflow.api.client;

import io.reactivex.rxjava3.core.Observable;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.client.models.PostClientsResponse;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * ClientApi interface for managing client-related operations in the Fineract system.
 * This interface defines methods for creating and activating clients.
 */
public interface ClientApi {
    @POST("clients")
    Observable<PostClientsResponse> createClient(@Body PostClientsRequest request);

    @POST("clients/{clientId}")
    Observable<Object> activateClient(@Path("clientId") Long clientId, @Body Object command);
}