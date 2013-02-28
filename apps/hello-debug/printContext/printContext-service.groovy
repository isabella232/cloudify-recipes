/*******************************************************************************
* Copyright (c) 2011 GigaSpaces Technologies Ltd. All rights reserved
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/

//TODO: put these debug definitions manually in a common jar file (or something similar)
//import static org.cloudifysource.Debug.*
import DebugHook

service {
    extend "../../../services/debug"
    name "printContext"
    type "APP_SERVER"

    compute {
        template "SMALL_UBUNTU"
    }

    lifecycle{
        preInstall (new DebugHook([context:context]).debug('sayHello.sh', "after"))
        install (new DebugHook([context:context]).debug('printContext.groovy'))

//        postInstall (new DebugHook([context:context]).debug(["printContext.groovy", "--help"],
//                                                       "onError"))
//
//        start (new DebugHook([context:context]).debug("printContext.groovy", "after"))
    }
}
