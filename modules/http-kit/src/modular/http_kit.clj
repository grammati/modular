;; Copyright © 2014, JUXT LTD. All Rights Reserved.
;;
;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at
;;
;;     http://www.apache.org/licenses/LICENSE-2.0
;;
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.

(ns modular.http-kit
  (:require
   modular.ring
   [com.stuartsierra.component :as component]
   [clojure.tools.logging :refer :all]
   [modular.ring :refer (handler)]
   [org.httpkit.server :refer (run-server)]))

(def default-port 8000)

(defrecord Webserver [port]
  component/Lifecycle
  (start [this]
    (if-let [ring-handler-provider (modular.ring/k this)]
      (let [h (handler ring-handler-provider)]
        (assert h)
        (if port
          (infof "port is %d" port)
          (warnf "port is nil, using default of %d" default-port))
        (assoc this :server (run-server h {:port (or port default-port)})))
      (throw (ex-info (format "http-kit module requires that entry %s be added to the system map by a component" key) {}))))

  (stop [this]
    (when-let [server (:server this)]
      (server)
      (dissoc this :server))))

;; Keep this around for integration with Prismatic Schema
(defn new-webserver [{:keys [port]}]
  (component/using
   (->Webserver port)
   [modular.ring/k]))
