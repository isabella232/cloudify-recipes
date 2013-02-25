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

//these are different entry points for creating a debug environment around a lifecycle script


//TODO!!: add flag to enable first trial run of the script, before/instead of entering debug
//def debug_hook(List    args) { return ['debug-hook.sh'] + args }
//def debug_hook(String  arg ) { return debug_hook([arg]) }
//def debug_hook(GString arg ) { return debug_hook([arg.toString()]) }
//def debug_hook(Map     args) { return args.inject([:]) {h, k ,v -> h[k] = debug_hook(v); h }}

service {
    extend "../../../services/debug"
    name "printContext"
    type "APP_SERVER"

    compute {
        template "SMALL_UBUNTU"
    }

    lifecycle{
        install (new DebugHook().debug_hook("printContext.groovy"))
    }
}
