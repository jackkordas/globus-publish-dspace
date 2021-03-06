/**
 * This file is a modified version of a DSpace file.
 * All modifications are subject to the following copyright and license.
 * 
 * Copyright 2016 University of Chicago. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.webui.submit.step;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.dspace.app.util.SubmissionInfo;
import org.dspace.app.webui.submit.JSPStep;
import org.dspace.app.webui.submit.JSPStepManager;
import org.dspace.app.webui.util.JSPManager;
import org.dspace.app.webui.util.UIUtil;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.Item;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.submit.lookup.SubmissionLookupDataLoader;
import org.dspace.submit.lookup.SubmissionLookupService;
import org.dspace.submit.step.SubmissionLookupStep;
import org.dspace.utils.DSpace;

/**
 * Step which controls selecting an item from external database service to auto
 * fill metadata for DSpace JSP-UI
 * <P>
 * This JSPStep class works with the SubmissionController servlet for the JSP-UI
 * <P>
 * The following methods are called in this order:
 * <ul>
 * <li>Call doPreProcessing() method</li>
 * <li>If showJSP() was specified from doPreProcessing(), then the JSP specified
 * will be displayed</li>
 * <li>If showJSP() was not specified from doPreProcessing(), then the
 * doProcessing() method is called an the step completes immediately</li>
 * <li>Call doProcessing() method on appropriate AbstractProcessingStep after
 * the user returns from the JSP, in order to process the user input</li>
 * <li>Call doPostProcessing() method to determine if more user interaction is
 * required, and if further JSPs need to be called.</li>
 * <li>If there are more "pages" in this step then, the process begins again
 * (for the new page).</li>
 * <li>Once all pages are complete, control is forwarded back to the
 * SubmissionController, and the next step is called.</li>
 * </ul>
 *
 * @see org.dspace.app.webui.servlet.SubmissionController
 * @see org.dspace.app.webui.submit.JSPStep
 * @see org.dspace.submit.step.StartSubmissionLookupStep
 *
 * @author Andrea Bollini
 * @version $Revision$
 */
public class JSPSubmissionLookupStep extends JSPStep
{
    /** JSP which displays HTML for this Class * */
    private static final String START_LOOKUP_JSP = "/submit/submission-lookup.jsp";

    /** log4j logger */
    private static Logger log = Logger
            .getLogger(JSPSubmissionLookupStep.class);

    SubmissionLookupService slService = new DSpace().getServiceManager()
            .getServiceByName(SubmissionLookupService.class.getCanonicalName(),
                    SubmissionLookupService.class);

    /**
     * Do any pre-processing to determine which JSP (if any) is used to generate
     * the UI for this step. This method should include the gathering and
     * validating of all data required by the JSP. In addition, if the JSP
     * requires any variable to passed to it on the Request, this method should
     * set those variables.
     * <P>
     * If this step requires user interaction, then this method must call the
     * JSP to display, using the "showJSP()" method of the JSPStepManager class.
     * <P>
     * If this step doesn't require user interaction OR you are solely using
     * Manakin for your user interface, then this method may be left EMPTY,
     * since all step processing should occur in the doProcessing() method.
     *
     * @param context
     *            current DSpace context
     * @param request
     *            current servlet request object
     * @param response
     *            current servlet response object
     * @param subInfo
     *            submission info object
     */
    public void doPreProcessing(Context context, HttpServletRequest request,
            HttpServletResponse response, SubmissionInfo subInfo)
            throws ServletException, IOException, SQLException,
            AuthorizeException
    {
        if (request.getAttribute("no.collection") == null
                || !(Boolean) request.getAttribute("no.collection"))
        {
            request.setAttribute("s_uuid", UUID.randomUUID().toString());
        }

        /*
         * Possible parameters from JSP:
         *
         * collection= <collection_id> - a collection that has already been
         * selected (to use as preference! it is not the final choice!!!)
         *
         * collectionid = the FINAL chosed collection!!!
         *
         * With no parameters, this servlet prepares for display of the Select
         * Collection JSP.
         */
       /* int collectionID = UIUtil.getIntParameter(request, "collectionid");
        Collection col = null;

        if (collectionID != -1)
        {
            col = Collection.find(context, collectionID);
        }

        if (col != null)
        {
            log.info("Select Collection page skipped, since a Collection ID was already found.  Collection ID=");
        }
        else
        {*/
            // gather info for JSP page
           /* Community com = UIUtil.getCommunityLocation(request);

            Collection[] collections;

            if (com != null)
            {
                // In a community. Show collections in that community only.
                collections = Collection.findAuthorized(context, com,
                        Constants.ADD);
            }
            else
            {
                // Show all collections
                collections = Collection.findAuthorized(context, null,
                        Constants.ADD);
            }

            // save collections to request for JSP
            request.setAttribute("collections", collections);
            */
            //request.setAttribute("collectionID", collectionID);

            Map<String, List<String>> identifiers2providers = slService
                    .getProvidersIdentifiersMap();
            List<String> searchProviders = slService.getSearchProviders();
            List<String> fileProviders = slService.getFileProviders();
            request.setAttribute("identifiers2providers", identifiers2providers);
            request.setAttribute("searchProviders", searchProviders);
            request.setAttribute("fileLoaders", fileProviders);
            request.setAttribute("identifiers", slService.getIdentifiers());
            // we need to load the select collection JSP
            JSPStepManager
                    .showJSP(request, response, subInfo, START_LOOKUP_JSP);

        //}
      }

    /**
     * Do any post-processing after the step's backend processing occurred (in
     * the doProcessing() method).
     * <P>
     * It is this method's job to determine whether processing completed
     * successfully, or display another JSP informing the users of any potential
     * problems/errors.
     * <P>
     * If this step doesn't require user interaction OR you are solely using
     * Manakin for your user interface, then this method may be left EMPTY,
     * since all step processing should occur in the doProcessing() method.
     *
     * @param context
     *            current DSpace context
     * @param request
     *            current servlet request object
     * @param response
     *            current servlet response object
     * @param subInfo
     *            submission info object
     * @param status
     *            any status/errors reported by doProcessing() method
     */
    public void doPostProcessing(Context context, HttpServletRequest request,
            HttpServletResponse response, SubmissionInfo subInfo, int status)
            throws ServletException, IOException, SQLException,
            AuthorizeException
    {
        // if the user didn't select a collection,
        // send him/her back to "select a collection" page
        /*if (status == StartSubmissionLookupStep.STATUS_NO_COLLECTION)
        {
            // specify "no collection" error message should be displayed
            request.setAttribute("no.collection", new Boolean(true));

            // reload this page, by re-calling doPreProcessing()
            doPreProcessing(context, request, response, subInfo);
        }
        else if (status == StartSubmissionLookupStep.STATUS_INVALID_COLLECTION)
        {
            JSPManager.showInvalidIDError(request, response,
                    request.getParameter("collectionid"), Constants.COLLECTION);
        }*/
        if (status == SubmissionLookupStep.STATUS_NO_SUUID)
        {
            // specify "no suuid" error message should be displayed
            request.setAttribute("no.suuid", new Boolean(true));

            // reload this page, by re-calling doPreProcessing()
            doPreProcessing(context, request, response, subInfo);
        }
        else if (status == SubmissionLookupStep.STATUS_SUBMISSION_EXPIRED)
        {
            // specify "no collection" error message should be displayed
            request.setAttribute("expired", new Boolean(true));

            // reload this page, by re-calling doPreProcessing()
            doPreProcessing(context, request, response, subInfo);
        }
        else if (status != SubmissionLookupStep.STATUS_COMPLETE)
        {
            // specify "no suuid" error message should be displayed
            request.setAttribute("no.suuid", new Boolean(true));

            // reload this page, by re-calling doPreProcessing()
            doPreProcessing(context, request, response, subInfo);
        }
    }

    /**
     * Return the URL path (e.g. /submit/review-metadata.jsp) of the JSP which
     * will review the information that was gathered in this Step.
     * <P>
     * This Review JSP is loaded by the 'Verify' Step, in order to dynamically
     * generate a submission verification page consisting of the information
     * gathered in all the enabled submission steps.
     *
     * @param context
     *            current DSpace context
     * @param request
     *            current servlet request object
     * @param response
     *            current servlet response object
     * @param subInfo
     *            submission info object
     */
    public String getReviewJSP(Context context, HttpServletRequest request,
            HttpServletResponse response, SubmissionInfo subInfo)
    {
        return NO_JSP; // at this time, you cannot review what collection you
                       // selected.
    }
}
