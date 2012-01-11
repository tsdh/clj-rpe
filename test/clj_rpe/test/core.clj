(ns clj-rpe.test.core
  (:use clj-rpe.core)
  (:use clojure.test))

(defn all-superclasses
  [c]
  (rpe-reachables c [rpe-+ 'getSuperclass]))

(deftest reflection-all-superclasses
  (is (= #{Number Object}
         (all-superclasses Long)))
  (is (= #{java.awt.Frame java.awt.Window java.awt.Container
           java.awt.Component java.lang.Object}
         (all-superclasses javax.swing.JFrame))))
