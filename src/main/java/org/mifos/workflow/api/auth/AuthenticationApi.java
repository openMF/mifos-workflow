package org.mifos.workflow.api.auth;

import io.reactivex.rxjava3.core.Observable;
import org.apache.fineract.client.models.PostAuthenticationRequest;
import org.apache.fineract.client.models.PostAuthenticationResponse;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;
/*
 * This interface defines the API for user authentication in the Mifos Workflow application.
 * It uses Retrofit to handle HTTP requests and responses.
 */
public interface AuthenticationApi {
    @POST("authentication")
    Observable<PostAuthenticationResponse> authenticate(
            @Body PostAuthenticationRequest request,
            @Query("externalId") boolean externalId);
}