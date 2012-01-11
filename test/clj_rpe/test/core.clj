(ns clj-rpe.test.core
  (:use clj-rpe.core)
  (:use clojure.test))

(defn superclasses
  [c]
  (rpe-reachables c [rpe-+ 'getSuperclass]))

(defn superclasses-reflexive
  [c]
  (rpe-reachables c [rpe-* 'getSuperclass]))

(defn supertypes
  [c]
  (rpe-reachables c [rpe-+ [rpe-alt 'getSuperclass
                                    'getInterfaces]]))

(defn returntypes
  "Returns the set of return types of c's methods."
  [c]
  (rpe-reachables c [rpe-seq 'getMethods 'getReturnType]))

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

;; (deftest reflection-returntypes
;;   (is (= #{void boolean java.lang.String int java.lang.Class}
;;          (returntypes Object))))
