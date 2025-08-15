package org.mifos.workflow.api.loan;

import io.reactivex.rxjava3.core.Observable;
import org.mifos.fineract.client.models.DeleteLoansLoanIdResponse;
import org.mifos.fineract.client.models.GetDelinquencyActionsResponse;
import org.mifos.fineract.client.models.GetDelinquencyTagHistoryResponse;
import org.mifos.fineract.client.models.GetLoansApprovalTemplateResponse;
import org.mifos.fineract.client.models.GetLoansLoanIdResponse;
import org.mifos.fineract.client.models.GetLoansResponse;
import org.mifos.fineract.client.models.GetLoansTemplateResponse;
import org.mifos.fineract.client.models.PostLoansDelinquencyActionResponse;
import org.mifos.fineract.client.models.PostLoansLoanIdResponse;
import org.mifos.fineract.client.models.PostLoansResponse;
import org.mifos.fineract.client.models.PutLoansLoanIdResponse;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.List;
import java.util.Map;

/**
 * LoansApi interface for managing loan-related operations in the Fineract system.
 * This interface defines methods for creating, retrieving, updating, and managing loans.
 */
public interface LoansApi {
    
    @POST("loans")
    Observable<PostLoansResponse> calculateLoanScheduleOrSubmitLoanApplication(
            @Body Map<String, Object> request,
            @Query("command") String command);
    
    @GET("loans/{loanId}")
    Observable<GetLoansLoanIdResponse> retrieveLoan(
            @Path("loanId") Long loanId,
            @Query("staffInSelectedOfficeOnly") Boolean staffInSelectedOfficeOnly,
            @Query("associations") String associations,
            @Query("exclude") String exclude,
            @Query("fields") String fields);
    
    @GET("loans/external-id/{loanExternalId}")
    Observable<GetLoansLoanIdResponse> retrieveLoanByExternalId(
            @Path("loanExternalId") String loanExternalId,
            @Query("staffInSelectedOfficeOnly") Boolean staffInSelectedOfficeOnly,
            @Query("associations") String associations,
            @Query("exclude") String exclude,
            @Query("fields") String fields);
    
    @POST("loans/{loanId}")
    Observable<PostLoansLoanIdResponse> stateTransitions(
            @Path("loanId") Long loanId,
            @Body Map<String, Object> request,
            @Query("command") String command);
    
    @POST("loans/external-id/{loanExternalId}")
    Observable<PostLoansLoanIdResponse> stateTransitionsByExternalId(
            @Path("loanExternalId") String loanExternalId,
            @Body Map<String, Object> request,
            @Query("command") String command);
    
    @PUT("loans/{loanId}")
    Observable<PutLoansLoanIdResponse> modifyLoanApplication(
            @Path("loanId") Long loanId,
            @Body Map<String, Object> request,
            @Query("command") String command);
    
    @PUT("loans/external-id/{loanExternalId}")
    Observable<PutLoansLoanIdResponse> modifyLoanApplicationByExternalId(
            @Path("loanExternalId") String loanExternalId,
            @Body Map<String, Object> request,
            @Query("command") String command);
    
    @POST("loans/glim/{glimId}")
    Observable<PostLoansLoanIdResponse> glimStateTransitions(
            @Path("glimId") Long glimId,
            @Body Map<String, Object> request,
            @Query("command") String command);
    
    @GET("loans/glim/{glimId}/repaymenttemplate")
    Observable<String> getGlimRepaymentTemplate(@Path("glimId") Long glimId);
    
    @GET("loans/{loanId}/delinquencytaghistory")
    Observable<List<GetDelinquencyTagHistoryResponse>> getDelinquencyTagHistory(@Path("loanId") Long loanId);
    
    @GET("loans/external-id/{loanExternalId}/delinquencytaghistory")
    Observable<List<GetDelinquencyTagHistoryResponse>> getDelinquencyTagHistoryByExternalId(@Path("loanExternalId") String loanExternalId);
    
    @GET("loans/{loanId}/delinquencyactions")
    Observable<List<GetDelinquencyActionsResponse>> getLoanDelinquencyActions(@Path("loanId") Long loanId);
    
    @GET("loans/external-id/{loanExternalId}/delinquencyactions")
    Observable<List<GetDelinquencyActionsResponse>> getLoanDelinquencyActionsByExternalId(@Path("loanExternalId") String loanExternalId);
    
    @POST("loans/{loanId}/delinquencyactions")
    Observable<PostLoansDelinquencyActionResponse> createLoanDelinquencyAction(
            @Path("loanId") Long loanId,
            @Body Map<String, Object> request);
    
    @POST("loans/external-id/{loanExternalId}/delinquencyactions")
    Observable<PostLoansDelinquencyActionResponse> createLoanDelinquencyActionByExternalId(
            @Path("loanExternalId") String loanExternalId,
            @Body Map<String, Object> request);
    
    @DELETE("loans/{loanId}")
    Observable<DeleteLoansLoanIdResponse> deleteLoanApplication(@Path("loanId") Long loanId);
    
    @DELETE("loans/external-id/{loanExternalId}")
    Observable<DeleteLoansLoanIdResponse> deleteLoanApplicationByExternalId(@Path("loanExternalId") String loanExternalId);
    
    @GET("loans/template")
    Observable<GetLoansTemplateResponse> getLoansTemplate(
            @Query("officeId") Long officeId,
            @Query("staffId") Long staffId,
            @Query("dateFormat") String dateFormat);
    
    @GET("loans/template")
    Observable<GetLoansTemplateResponse> template10(
            @Query("clientId") Long clientId,
            @Query("groupId") Long groupId,
            @Query("productId") Long productId,
            @Query("templateType") String templateType,
            @Query("staffInSelectedOfficeOnly") Boolean staffInSelectedOfficeOnly,
            @Query("activeOnly") Boolean activeOnly);
    
    @Multipart
    @POST("loans/template")
    Observable<String> postLoanTemplate(
            @Part("dateFormat") String dateFormat,
            @Part("locale") String locale,
            @Part("uploadedInputStream") Object uploadedInputStream);
    
    @Multipart
    @POST("loans/repaymenttemplate")
    Observable<String> postLoanRepaymentTemplate(
            @Part("dateFormat") String dateFormat,
            @Part("locale") String locale,
            @Part("uploadedInputStream") Object uploadedInputStream);
    
    @GET("loans")
    Observable<GetLoansResponse> retrieveAllLoans(
            @Query("externalId") String externalId,
            @Query("offset") Integer offset,
            @Query("limit") Integer limit,
            @Query("orderBy") String orderBy,
            @Query("sortOrder") String sortOrder,
            @Query("accountNo") String accountNo,
            @Query("status") String status);
    
    @GET("loans/{loanId}/approvaltemplate")
    Observable<GetLoansApprovalTemplateResponse> retrieveApprovalTemplate(
            @Path("loanId") Long loanId,
            @Query("templateType") String templateType);
    
    @GET("loans/external-id/{loanExternalId}/approvaltemplate")
    Observable<GetLoansApprovalTemplateResponse> retrieveApprovalTemplateByExternalId(
            @Path("loanExternalId") String loanExternalId,
            @Query("templateType") String templateType);
    
    @GET("loans/repaymenttemplate")
    Observable<Void> getLoanRepaymentTemplate(
            @Query("officeId") Long officeId,
            @Query("dateFormat") String dateFormat);
}





