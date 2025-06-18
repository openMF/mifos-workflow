package org.mifos.workflow.api.client;

import io.reactivex.rxjava3.core.Observable;
import org.mifos.fineract.client.models.*;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import org.mifos.workflow.dto.fineract.office.OfficeDTO;
import org.mifos.workflow.dto.fineract.code.CodeData;

import java.util.List;
import java.util.Map;

/**
 * ClientsApi interface for managing client-related operations in the Fineract system.
 * This interface defines methods for creating, retrieving, updating, and managing clients.
 */
public interface ClientsApi {
    @POST("clients")
    Observable<PostClientsResponse> createClient(@Body Map<String, Object> request);

    @GET("clients/{clientId}")
    Observable<GetClientsClientIdResponse> retrieveClient(@Path("clientId") Long clientId);

    @GET("clients")
    Observable<GetClientsResponse> retrieveAllClients(
            @Query("officeId") Long officeId,
            @Query("searchText") String searchText,
            @Query("status") String status,
            @Query("limit") Integer limit,
            @Query("offset") Integer offset);

    @PUT("clients/{clientId}")
    Observable<PutClientsClientIdResponse> updateClient(
            @Path("clientId") Long clientId,
            @Body Map<String, Object> request);

    @POST("clients/{clientId}")
    @Headers("Content-Type: application/json")
    Observable<PostClientsClientIdResponse> activateClient(
            @Path("clientId") Long clientId,
            @Query("command") String command,
            @Body Map<String, Object> request);

    @GET("clients/{clientId}/transferproposaldate")
    Observable<GetClientTransferProposalDateResponse> retrieveTransferTemplate(@Path("clientId") Long clientId);

    @POST("clients/{clientId}")
    @Headers("Content-Type: application/json")
    Observable<PostClientsClientIdResponse> applyCommand(
            @Path("clientId") String clientId,
            @Body Map<String, Object> request,
            @Query("command") String command);

    @GET("offices")
    Observable<List<OfficeDTO>> retrieveAllOffices();

    @GET("codes")
    Observable<List<CodeData>> retrieveCodes();

    @GET("codes/{codeId}/codevalues")
    Observable<List<CodeValueData>> retrieveAllCodeValues(@Path("codeId") Long codeId);

    @POST("codes/{codeId}/codevalues")
    Observable<CodeValueData> createCodeValue(@Path("codeId") Long codeId, @Body Map<String, Object> request);

}