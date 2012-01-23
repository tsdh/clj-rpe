(ns clj-rpe.test.core
  (:use clj-rpe.core)
  (:use clojure.test))

(def m {:a 1
        "b" {1 "One", 2 "Two", 3 "Three"}
        :c {:foo {:x :foox, :y :fooy}
            :bar {:x :barx, :y :bary}
            :baz {:x :bazx, :y :bazy, :z :bazz}}
        :x {:y {:x {:y {:x {:y "Got me!"}}}}}})

(deftest map-tests
  (is (= #{1}
         (rpe m :a)))
  (is (= #{}
         (rpe m :unknown)))
  (is (= #{{1 "One", 2 "Two", 3 "Three"}}
         (rpe m "b")))
  (is (= #{:bary}
         (rpe m [rpe-seq :c :bar :y])))
  (is (= #{{:y :bary, :x :barx} :bary}
         (rpe m [rpe-seq :c :bar [rpe-opt :y]])))
  (is (= #{:fooy :bary :bazy}
         (rpe m [rpe-seq :c [rpe-alt :foo :bar :baz] :y])))
  (is (= #{{:x {:y {:x {:y "Got me!"}}}}
           {:x {:y "Got me!"}}
           "Got me!"}
         (rpe m [rpe-+ [rpe-seq :x :y]])))
  (is (= #{"Got me!"}
         (rpe m [rpe-exp 3 [rpe-seq :x :y]])))
  (is (= #{{:x {:y "Got me!"}} "Got me!"}
         (rpe m [rpe-exp 2 19 [rpe-seq :x :y]])))
  (is (= #{"Got me!"}
         (rpe m [rpe-seq [rpe-+ [rpe-seq :x :y]]
                         [rpe-restr string?]]))))

(deftest iterator-test
  (is (= #{1 2 3}
         (rpe [[1 2 3 2 1]] 'iterator))))

(deftest enumeration-test
  (is (= #{"foo" "bar" "baz"}
         (rpe [(java.util.Vector. ["foo" "bar" "foo" "baz" "bar"])]
              'elements))))

(defn superclasses
  [c]
  (rpe c [rpe-+ 'getSuperclass]))

(defn superclasses-reflexive
  [c]
  (rpe c [rpe-* 'getSuperclass]))

(defn supertypes
  [c]
  (rpe c [rpe-+ [rpe-alt 'getSuperclass
                         'getInterfaces]]))

(defn returntypes
  "Returns the set of return types of c's methods."
  [c]
  (rpe c [rpe-seq 'getMethods 'getReturnType]))

(deftest reflection-superclasses
  (is (= #{}
         (superclasses Object)))
  (is (= #{Number Object}
         (superclasses Long)))
  (is (= #{Long Number Object}
         (superclasses-reflexive Long)))
  (is (= #{java.awt.Frame java.awt.Window java.awt.Container
           java.awt.Component java.lang.Object}
         (superclasses javax.swing.JFrame)))
  (is (= #{javax.swing.JFrame java.awt.Frame java.awt.Window
           java.awt.Container java.awt.Component java.lang.Object}
         (superclasses-reflexive javax.swing.JFrame))))

(deftest reflection-supertypes
  (is (= #{}
         (supertypes Object)))
  (is (= #{java.lang.Number java.lang.Comparable
           java.lang.Object java.io.Serializable}
         (supertypes Long))))

(deftest reflection-returntypes
  (is (= #{(Void/TYPE) (Boolean/TYPE) java.lang.String
           (Integer/TYPE) java.lang.Class}
         (returntypes Object))))

(defprotocol Successor
  (succ [this]))

(deftype Int [val]
  Successor
  (succ [_]
    (Int. (inc val))))
