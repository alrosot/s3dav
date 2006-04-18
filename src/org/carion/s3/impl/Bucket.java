/*
 * Copyright (c) 2006, Pierre Carion.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.carion.s3.impl;

import java.util.Date;

public class Bucket {
    private final String _name;

    private final Date _creationDate;

    public Bucket(String name, Date creationDate) {
        _name = name;
        _creationDate = creationDate;
    }

    public String getName() {
        return _name;
    }

    public Date getCreationDate() {
        return _creationDate;
    }
}
