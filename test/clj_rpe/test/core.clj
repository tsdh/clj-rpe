(ns clj-rpe.test.core
  (:use clj-rpe.core)
  (:use clojure.test))

(defn all-superclasses
  [c]
  (rpe-reachables c [rpe-+ 'getSuperclass]))

(defn all-superclasses-reflexive
  [c]
  (rpe-reachables c [rpe-* 'getSuperclass]))

(deftest reflection-all-superclasses
  (is (= #{Number Object}
         (all-superclasses Long)))
  (is (= #{Long Number Object}
         (all-superclasses-reflexive Long)))
  (is (= #{java.awt.Frame java.awt.Window java.awt.Container
           java.awt.Component java.lang.Object}
         (all-superclasses javax.swing.JFrame)))
  (is (= #{javax.swing.JFrame java.awt.Frame java.awt.Window
           java.awt.Container java.awt.Component java.lang.Object}
         (all-superclasses-reflexive javax.swing.JFrame))))
