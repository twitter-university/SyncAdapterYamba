/* $Id: $
   Copyright 2012, G. Blake Meike

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.twitter.university.android.yamba.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 *
 * @version $Revision: $
 * @author <a href="mailto:blake.meike@gmail.com">G. Blake Meike</a>
 */
public class ProjectionMap {

    /**
     * Builder
     */
    public static class Builder {
        final Map<String, String> colMap = new HashMap<String, String>();

        /**
         * @param virtCol the virtual column name
         * @param actCol the actual column name
         * @return the builder
         */
        public Builder addColumn(String virtCol, String actCol) {
            colMap.put(virtCol, actCol + " AS " + virtCol);
            return this;
        }

        /**
         * @param virtCol the virtual column name
         * @param actTable the target table
         * @param actCol the actual column name
         * @return the builder
         */
        public Builder addColumn(String virtCol, String actTable, String actCol) {
            return addColumn(virtCol, actTable + "." + actCol);
        }

        /**
         * @return the column map
         */
        public ProjectionMap build() { return new ProjectionMap(colMap); }
    }


    private final Map<String, String> colMap;

    ProjectionMap(Map<String, String> colMap) {
        this.colMap = Collections.unmodifiableMap(colMap);
    }

    /**
     * @return the projection map
     */
    public Map<String, String> getProjectionMap() { return colMap; }
}
