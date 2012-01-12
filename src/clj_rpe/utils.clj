(ns clj-rpe.utils
  (:use ordered.set)
  (:import [java.util Collection])
  (:import [java.lang.reflect Method Field]))

(defn into-oset
  ([x]
     (cond
      (nil? x)                 (ordered-set)
      (set? x)                 x
      (instance? Collection x) (apply ordered-set (seq x))
      (.isArray (class x))     (apply ordered-set (seq x))
      :else                    (ordered-set x)))
  ([s x]
     (into (into-oset s) (into-oset x))))

(defn invoke-method
  [o mname]
  (try
    (into-oset
     (clojure.lang.Reflector/invokeInstanceMethod
      o (name mname) (to-array [])))
    (catch Exception _ (ordered-set))))

(defn access-field
  [o fname]
  (try
    (into-oset
     (clojure.lang.Reflector/getInstanceField
      o (name fname)))
    (catch Exception _ (ordered-set))))
