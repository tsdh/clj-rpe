(ns clj-rpe.core
  (:use clj-rpe.utils)
  (:require [clojure.set]))

(defprotocol Nodes
  (--> [this rpd]))

(extend-protocol Nodes
  java.util.Map
  (--> [this rpd]
    (into-oset (get this rpd)))
  java.lang.Object
  (--> [this rpd]
    (cond
     (keyword? rpd) (access-field this rpd)
     (symbol? rpd)  (invoke-method this rpd)
     (fn? rpd)      (into-oset (rpd this))
     :else (throw (RuntimeException.
                   (format "Unsupported rpd type %s" (type rpd)))))))

(defn rpe
  "Returns the set of objects reachable from `objs' (an object or seq of
  objects) via the path description `rpd'.

  Example: Get all direct and indirect supertypes (superclasses, interfaces)
  of Long.

    (rpe Long [rpe-+ [rpe-alt 'getSuperclass
                              'getInterfaces]])"
  [objs rpd]
  (cond
   (fn? rpd)   (into-oset (mapcat (fn [x]
                                    (into-oset
                                     (try
                                       (rpd x)
                                       (catch clojure.lang.ArityException e
                                         (throw e))
                                       (catch Exception _ nil))))
                                  (into-oset objs)))
   (coll? rpd) (apply (first rpd) objs (rest rpd))
   :else       (into-oset (mapcat #(--> % rpd) (into-oset objs)))))

(defn rpe-seq
  "Regular path sequence starting at `objs' and traversing a path defined by
  `rpds'."
  [objs & rpds]
  (let [ss (into-oset objs)]
    (if (seq rpds)
      (recur (rpe ss (first rpds))
             (rest rpds))
      ss)))

(defn rpe-alt
  "Regular path alternative starting at `objs' and traversing all `alts' from
  there."
  [objs & alts]
  (into-oset (mapcat #(rpe objs %) alts)))

(defn rpe-opt
  "Regular path option returning the union of `objs' and the objects reachable
  via `rpd'."
  [objs rpd]
  (let [ss (into-oset objs)]
    (into-oset ss (rpe ss rpd))))

(defn rpe-+
  "Regular path iteration starting at `objs' iterating `rpd' one or many
  times."
  ([objs rpd]
     (rpe-+ objs rpd false true))
  ([objs rpd d skip-objs]
     (let [objs (into-oset objs)
	   n    (rpe (if (false? d) objs d) rpd)
	   df   (clojure.set/difference n objs)
	   sv   (if skip-objs n (into-oset objs n))]
       (if (seq df)
	 (recur sv rpd df false)
         sv))))

(defn rpe-*
  "Regular path iteration starting at `objs' iterating `rpd' zero or many
  times."
  [objs rpd]
  (rpe-+ objs rpd false false))

(defn rpe-exp
  "Regular path iteration starting at `objs' iterating `rpd' either exactly `n'
  times, or at minimum `l' times and at most `u' times."
  ([objs l u rpd]
     {:pre [(<= l u) (>= l 0) (>= u 0)]}
     (loop [i (- u l), s (rpe-exp objs l rpd)]
       (if (pos? i)
         (let [ns (into s (rpe s rpd))]
           (if (= (count s) (count ns))
             s
             (recur (dec i) ns)))
         s)))
  ([objs n rpd]
     {:pre [(>= n 0)]}
     (if (zero? n)
       (into-oset objs)
       (recur (rpe objs rpd) (dec n) rpd))))

(defn rpe-restr
  "Regular path restriction filtering `objs' by `pred'."
  [objs pred]
  (into-oset (filter pred objs)))

