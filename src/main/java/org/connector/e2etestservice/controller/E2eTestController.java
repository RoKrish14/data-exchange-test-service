/********************************************************************************
 * Copyright (c) 2023 T-Systems International GmbH
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

package org.connector.e2etestservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.connector.e2etestservice.model.ConnectorTestRequest;
import org.connector.e2etestservice.model.OwnConnectorTestRequest;
import org.connector.e2etestservice.service.TestConnectorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
public class E2eTestController {

    private String testConnectorUrl;
    private String testConnectorApiKeyHeader;
    private String testConnectorApiKey;

    private TestConnectorService testConnectorService;

    @Autowired
    public E2eTestController(TestConnectorService testConnectorService,
                             @Value("${default.edc.hostname}") String testConnectorUrl,
                             @Value("${default.edc.apiKeyHeader}") String testConnectorApiKeyHeader,
                             @Value("${default.edc.apiKey}") String testConnectorApiKey) {
        this.testConnectorService = testConnectorService;
        this.testConnectorUrl = testConnectorUrl;
        this.testConnectorApiKeyHeader = testConnectorApiKeyHeader;
        this.testConnectorApiKey = testConnectorApiKey;
    }

    @Operation(summary = "Request to test connector",
            description = "Request to test connector.")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = String.class)))
            }
    )
    @PostMapping("/connector-test")
    public Object testConnector(@Valid @RequestBody ConnectorTestRequest connectorTestRequest) {
        Map<String, String> result = new HashMap<>();
        ConnectorTestRequest preconfiguredTestConnector = ConnectorTestRequest.builder()
                .connectorHost(testConnectorUrl)
                .apiKeyHeader(testConnectorApiKeyHeader)
                .apiKeyValue(testConnectorApiKey)
                .build();
        boolean consumerTestResult = testConnectorService.testConnectorConnectivity(connectorTestRequest, preconfiguredTestConnector);
        boolean providerTestResult = testConnectorService.testConnectorConnectivity(preconfiguredTestConnector, connectorTestRequest);
        if(consumerTestResult && providerTestResult) {
            result.put("message", "Connector is working as a consumer and provider");
            return new ResponseEntity(result, HttpStatus.OK);
        } else {
            result.put("message", "Connector is not working properly");
            return new ResponseEntity(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/own-connector-test")
    public Object testOwnConnector(@Valid @RequestBody OwnConnectorTestRequest ownConnectorTestRequest) {
        Map<String, String> result = new HashMap<>();
        ConnectorTestRequest firstConnector = ConnectorTestRequest.builder()
                .connectorHost(ownConnectorTestRequest.getFirstConnectorHost())
                .apiKeyHeader(ownConnectorTestRequest.getFirstApiKeyHeader())
                .apiKeyValue(ownConnectorTestRequest.getFirstApiKeyValue())
                .build();

        ConnectorTestRequest secondConnector = ConnectorTestRequest.builder()
                .connectorHost(ownConnectorTestRequest.getSecondConnectorHost())
                .apiKeyHeader(ownConnectorTestRequest.getSecondApiKeyHeader())
                .apiKeyValue(ownConnectorTestRequest.getSecondApiKeyValue())
                .build();
        boolean consumerTestResult = testConnectorService.testConnectorConnectivity(secondConnector, firstConnector);
        boolean providerTestResult = testConnectorService.testConnectorConnectivity(firstConnector, secondConnector);
        if(consumerTestResult && providerTestResult) {
            result.put("message", "Connector is working as a consumer and provider");
            return new ResponseEntity(result, HttpStatus.OK);
        } else {
            result.put("message", "Connector is not working properly");
            return new ResponseEntity(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
