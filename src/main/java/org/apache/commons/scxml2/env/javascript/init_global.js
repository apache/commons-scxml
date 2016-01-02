/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
    Define global and protected SCXML system properties and builtin functions,
    all delegating to _scxmlSystemContext variables map provided as a ScriptEngine binding.
    The _event object is wrapped in additional (frozen) _scxmlEvent property to provide ECMAScript object semantics.
 */
Object.defineProperties(this, {
    // common method to throw 'protected' error
    "_scxmlProtected":
    {
        get: function() {
            return function(name) {
                throw new Error(name+" is a protected SCXML system property")
            }
        }
    },
    "_name": {
        get: function () {
            return _scxmlSystemContext._name
        },
        set: function () {
            _scxmlProtected("_name")
        }
    },
    "_sessionid":
    {
        get: function() {
            return _scxmlSystemContext._sessionid
        },
        set: function() {
            _scxmlProtected("_sessionid")
        }
    },
    "_ioprocessors":
    {
        get: function() {
            return _scxmlSystemContext._ioprocessors
        },
        set: function() {
            _scxmlProtected("_ioprocessors")
        }
    },
    // extra wrapper object needed for _event wrapping as defining this inline
    // on _event.get method somehow doesn't work properly
    "_scxmlEvent":
    {
        value: {
            get name() {
                return _scxmlSystemContext._event.name||undefined
            },
            set name(val) {
                _scxmlProtected("_event.name")
            },
            get type() {
                return _scxmlSystemContext._event.type||undefined
            },
            set type(val) {
                _scxmlProtected("_event.type")
            },
            get sendid() {
                return _scxmlSystemContext._event.sendid||undefined
            },
            set sendid(val) {
                _scxmlProtected("_event.sendid")
            },
            get orgin() {
                return _scxmlSystemContext._event.orgin||undefined
            },
            set origin(val) {
                _scxmlProtected("_event.origin")
            },
            get origintype() {
                return _scxmlSystemContext._event.orgintype||undefined
            },
            set origintype(val) {
                _scxmlProtected("_event.origintype")
            },
            get invokeid() {
                return _scxmlSystemContext._event.invokeid||undefined
            },
            set invokeid(val) {
                _scxmlProtected("_event.invokeid")
            },
            get data() {
                return _scxmlSystemContext._event.data||undefined
            },
            set data(val) {
                _scxmlProtected("_event.data")
            }
        },
        writable : false,
        configurable : false,
        enumeratable : false,
    },
    "_event":
    {
        get: function() {
            return _scxmlSystemContext._event ? _scxmlEvent : undefined;
        },
        set: function() {
            _scxmlProtected("_event")
        }
    },
    "_x":
    {
        get: function() {
            return _scxmlSystemContext._x
        },
        set: function() {
            _scxmlProtected("_x")
        }
    },
    // required SCXML builtin In() predicate
    "In":
    {
        get: function() {
            return function(state) {
                return _scxmlSystemContext._x.status.isInState(state)
            }
        },
        set: function() {
            _scxmlProtected("_In()")
        }
    }
});
// ensure extra _scxmlEvent wrapper object is deep protected
Object.freeze(_scxmlEvent);
