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
package org.carion.s3dav;

/**
 * 0.5
 * It's now possible to use spaces or '&' in the names
 * of files and directories.
 *
 * In the buckets page of the admin console (http://127.0.0.1:8070/index.html?page=buckets)
 * you can know see the 'raw listings' of all the objects
 * which have been stored in your buckets (even the objects
 * not created with s3DAV). You then have a way to delete
 * those objects.
 *
 * There is now a cache mechanism used to cache the 'HEAD' requests.
 * This should improve the performances as a lot of 'HEAD' request
 * are done to get the status of the file system
 */
public class Version {
    public final static String VERSION = "0.5";

    public final static String USER_AGENT = "s3DAV/" + VERSION;
}
