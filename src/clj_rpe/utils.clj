(ns clj-rpe.utils
  (:use ordered.set)
  (:import [java.util Collection])
  (:import [java.lang.reflect Method Field]))

(defn into-oset
  "Return an ordered set representation of `x' or the union of `s' and `x'.
  Single-valued objects get conjoined, collections and arrays are converted to
  ordered sets."
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
  "Returns an ordered set of `o's `mname' instance method's return value.
  If there's no such method, return an empty ordered set."
  [o mname]
  (try
    (into-oset
     (clojure.lang.Reflector/invokeInstanceMethod
      o (name mname) (to-array [])))
    (catch Exception _ (ordered-set))))

(defn access-field
  "Return an ordered set of `o's `fname' field value.
  If there's no such field, return an empty ordered set."
  [o fname]
  (try
    (into-oset
     (clojure.lang.Reflector/getInstanceField
      o (name fname)))
    (catch Exception _ (ordered-set))))
