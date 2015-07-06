;; Copyright 2014 Red Hat, Inc, and individual contributors.
;;
;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at
;;
;; http://www.apache.org/licenses/LICENSE-2.0
;;
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.

(ns wunderboss.messaging-tests
  (:require [clojure.test :refer :all])
  (:import [org.projectodd.wunderboss Option WunderBoss]
           [org.projectodd.wunderboss.codecs Codecs None StringCodec]
           [org.projectodd.wunderboss.messaging Messaging
            Messaging$CreateQueueOption
            Context Destination Destination$ListenOption
            Destination$ReceiveOption
            MessageHandler]
           wunderrabbit.RabbitMessagingProvider))

(WunderBoss/registerComponentProvider Messaging (RabbitMessagingProvider.))

(def default (doto (WunderBoss/findOrCreateComponent Messaging) (.start)))

(def codecs (doto (Codecs.) (.add None/INSTANCE)))

(defn create-opts-fn [class]
  ;; clojure 1.7.0 no longer initializes classes on import, so we have
  ;; to force init here (see CLJ-1315)
  (Class/forName (.getName class))
  (let [avail-options (->> class
                        Option/optsFor
                        (map #(vector (keyword (.name %)) %))
                        (into {}))]
    (fn [opts]
      (reduce (fn [m [k v]]
                (assoc m
                  (if-let [enum (avail-options k)]
                    enum
                    (throw (IllegalArgumentException. (str k " is not a valid option."))))
                  v))
        {}
        opts))))

(def coerce-queue-options (create-opts-fn Messaging$CreateQueueOption))
(def coerce-listen-options (create-opts-fn Destination$ListenOption))
(def coerce-receive-options (create-opts-fn Destination$ReceiveOption))

(deftest queue-creation-publish-receive-close
  (with-open [context (.createContext default nil)]
    (let [queue (.findOrCreateQueue default "a-queue"
                  (coerce-queue-options {:context context}))]

      ;; we should be able to publish and rcv
      (.publish queue "hi" None/INSTANCE nil)
      (let [msg (.receive queue codecs (coerce-receive-options {:timeout 1000}))]
        (is msg)
        (is (= "hi" (.body msg)))))

    ;; a closed endpoint should no longer be avaiable
    ;;(.close endpoint)
    ;; (is (thrown? javax.jms.InvalidDestinationException
    ;;       (.receive connection endpoint (coerce-receive-options {:timeout 1}))))
    ))

(deftest listen
  (with-open [context (.createContext default nil)]
    (let [queue (.findOrCreateQueue default "listen-queue"
                  (coerce-queue-options {:context context}))
          called (atom (promise))
          listener (.listen queue
                     (reify MessageHandler
                       (onMessage [_ msg _]
                         (deliver @called (.body msg))
                         nil))
                     codecs
                     nil)]
      (.publish queue "hi" None/INSTANCE nil)
      (is (= "hi" (deref @called 1000 :failure)))

      (reset! called (promise))
      (.close listener)
      (.publish queue "hi" None/INSTANCE nil)
      (is (= :success (deref @called 1000 :success))))))

#_(deftest request-response
  (with-open [connection (.createConnection default nil)]
    (let [queue (.findOrCreateEndpoint default "listen-queue" nil)
          listener (.listen connection queue
                     (reify MessageHandler
                       (onMessage [_ msg]
                         (.reply msg (.body msg String) nil nil)))
                     (coerce-listen-options {:selector "synchronous = TRUE"}))
          response (.request connection queue "hi" nil nil)]
      (is (= "hi" (.body (.get response) String)))
      ;; result should be cached
      (is (= "hi" (.body (.get response) String))))))
