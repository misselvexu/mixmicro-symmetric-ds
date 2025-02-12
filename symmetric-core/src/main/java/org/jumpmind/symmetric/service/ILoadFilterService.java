/**
 * Licensed to JumpMind Inc under one or more contributor
 * license agreements.  See the NOTICE file distributed
 * with this work for additional information regarding
 * copyright ownership.  JumpMind Inc licenses this file
 * to you under the GNU General Public License, version 3.0 (GPLv3)
 * (the "License"); you may not use this file except in compliance
 * with the License.
 *
 * You should have received a copy of the GNU General Public License,
 * version 3.0 (GPLv3) along with this library; if not, see
 * <http://www.gnu.org/licenses/>.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jumpmind.symmetric.service;

import java.util.List;
import java.util.Map;

import org.jumpmind.symmetric.model.LoadFilter;
import org.jumpmind.symmetric.model.LoadFilter.LoadFilterType;
import org.jumpmind.symmetric.model.NodeGroupLink;
import org.jumpmind.symmetric.service.impl.LoadFilterService.LoadFilterNodeGroupLink;

public interface ILoadFilterService {
    public Map<LoadFilterType, Map<String, List<LoadFilter>>> findLoadFiltersFor(NodeGroupLink link, boolean useCache);

    public List<LoadFilterNodeGroupLink> getLoadFilterNodeGroupLinks();

    public void saveLoadFilter(LoadFilterNodeGroupLink loadFilter);

    public void deleteLoadFilter(String loadFilterId);

    public void clearCache();

    public boolean refreshFromDatabase();
}
