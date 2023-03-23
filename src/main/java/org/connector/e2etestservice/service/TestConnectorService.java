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

package org.connector.e2etestservice.service;

import lombok.extern.slf4j.Slf4j;
import org.connector.e2etestservice.api.DataOfferCreationProxy;
import org.connector.e2etestservice.api.QueryDataOffersProxy;
import org.connector.e2etestservice.facilitator.ConnectorFacilitator;
import org.connector.e2etestservice.model.ConnectorTestRequest;
import org.connector.e2etestservice.model.contractoffers.ContractOffersCatalogResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TestConnectorService {

    private ConnectorFacilitator connectorFacilitator;

    private QueryDataOffersProxy queryDataOffersProxy;

    private DataOfferCreationProxy dataOfferCreationProxy;
    private DataOfferService dataOfferService;

    @Autowired
    public TestConnectorService(ConnectorFacilitator connectorFacilitator,
                                QueryDataOffersProxy queryDataOffersProxy,
                                DataOfferCreationProxy dataOfferCreationProxy,
                                DataOfferService dataOfferService) {
        this.connectorFacilitator = connectorFacilitator;
        this.queryDataOffersProxy = queryDataOffersProxy;
        this.dataOfferCreationProxy = dataOfferCreationProxy;
        this.dataOfferService = dataOfferService;
    }

    public boolean testConnectorConnectivity(ConnectorTestRequest providerConnectorRequest,
                                           ConnectorTestRequest consumerTestConnector) {
        try {
            // 1. Create data offer for provider
            dataOfferService.createDataOfferForTesting(providerConnectorRequest);

            // 3. Fetch newly created data offer from preconfigured test connector
            ContractOffersCatalogResponse contractOfferCatalog = connectorFacilitator.getContractOfferFromConnector(
                    consumerTestConnector,
                    providerConnectorRequest
            );

            return contractOfferCatalog != null && !contractOfferCatalog.getContractOffers().isEmpty();

        } catch (Exception e) {
            log.info("Exception occurred while testing connector as a provider"+ e);
            return false;
        }
    }

}
