(ns clj-rpe.core
  (:use clj-rpe.utils)
  (:use ordered.set)
  (:require clojure.set))

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
  objects) by via the path description `rpd'."
  [v rpd]
  (cond
   (fn? rpd)   (into-oset (mapcat #(rpd %) (into-oset v)))
   (coll? rpd) (apply (first rpd) v (rest rpd))
   :else       (into-oset (mapcat #(--> % rpd) (into-oset v)))))

(defn rpe-seq
  [start & specs]
  (let [ss (into-oset start)]
    (if (seq specs)
      (recur (rpe ss (first specs))
             (rest specs))
      ss)))

(defn rpe-alt
  [start & alts]
  (into-oset (mapcat #(rpe start %) alts)))

(defn rpe-opt
  [start rpd]
  (let [ss (into-oset start)]
    (into-oset ss (rpe ss rpd))))

(defn rpe-+
  ([v p]
     (rpe-+ v p false true))
  ([v p d skip-v]
     (let [v  (into-oset v)
	   n  (rpe (if (false? d) v d) p)
	   df (clojure.set/difference n v)
	   sv (if skip-v n (into-oset v n))]
       (if (seq df)
	 (recur sv p df false)
         sv))))

(defn rpe-*
  [objs rpd]
  (rpe-+ objs rpd false false))

(defn rpe-exp
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
  [objs pred]
  (into-oset (filter pred objs)))

