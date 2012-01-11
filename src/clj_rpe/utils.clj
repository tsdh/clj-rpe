(ns clj-rpe.utils
  (:use ordered.set)
  (:import [java.util Collection]))

(defn into-oset
  ([x]
     (cond
      (nil? x)                 (ordered-set)
      (set? x)                 x
      (instance? Collection x) (apply ordered-set (seq x))
      :else                    (ordered-set x)))
  ([s x]
     (into (into-oset s) (into-oset x))))

(defn invoke-method
  [o mname]
  (let [c (class o)
        mn (name mname)
        m (first (filter #(and (= (.getName %) mn)
                               (== 0 (alength (.getParameterTypes %))))
                         (.getMethods c)))]
    (into-oset
     (when m
       (.invoke m o (to-array []))))))
