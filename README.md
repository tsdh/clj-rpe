# clj-rpe

Clj-rpe is a Clojure library that implements *Regular Path Expressions* (RPEs)
on Clojure data structures and arbitrary Java objects.  It enables you to
retrieve the ordered set of objects reachable from a given start object (or set
of start objects) by traversing a path conforming to a *regular path
description*.

A regular path description is a declarative means to specify allowed paths in
some object structure in terms of keys in a map or fields/methods in classes.
Those can be composed using regular operators such as sequence, option,
alternative, and iterations.

You can get this library from [Clojars](http://clojars.org/clj-rpe).

RPEs were originally to conceived and implemented for graphs by the working
group for the graph query language
[GReQL](http://www.uni-koblenz-landau.de/koblenz/fb4/institute/IST/RGEbert/MainResearch-en/Graphtechnology/graph-repository-query-language-greql).
However, one can view any map as a graph, where the keys are the edges and the
values are the nodes.  Likewise, one can view any object net as a graph, where
the field and method names are the edges, and the field values and objects
returned by methods are the nodes.  And finally, any Clojure function of arity
one can be viewed as an edge, and its return value is the node at the other
side.

## Usage

Add a `(:use clj-rpe.core)` to your namespace declaration, and you are ready to
go.

Clj-rpe works for Clojure data structures (maps, records) and arbitrary Java
objects.  We'll start with the former.  For demonstration purposes, we'll
define some map `m`.  `m` could also be a record, because that's essentially a
map as well.

    (def m {:a 1
            "b" {1 "One", 2 "Two", 3 "Three"}
            :c {:foo {:x :foox, :y :fooy}
                :bar {:x :barx, :y :bary}
                :baz {:x :bazx, :y :bazy, :z :bazz}}
            :x {:y {:x {:y {:x {:y "Got me!"}}}}}})

The main function of clj-rpe is `rpe`.  It gets a start object (or a
collection of start objects) and a regular path description and returns the
ordered set of reachable objects.  In case of a map (or record), the most
simple path description is just a key that's looked up in the map.

What objects can be reached from `m` by traversing a path consisting only of
the key `:a`?

    (rpe m :a)
    ;=> #{1}
    (rpe m "b")
    ;=> #{{1 "One", 2 "Two", 3 "Three"}}

What happens when we use a key that's not in the map?

    (rpe m 'unkn0wn)
    ;=> #{}

We cannot reach anything inside `m` with that key, so the returned set is
empty.

### Regular Path Descriptions

Building upon the simple path descriptions (aka, keys in a map), we can define
regular path descriptions using the operators discussed in this section.

**Sequence.** The function `rpe-seq` is the path sequence returning the objects
reachable by traversing one path description after the other.  It is a
function, but usually you invoke it thru `rpe`.

    (rpe m [rpe-seq :c :bar :y])
    ;=> #{:bary}

**Option.** The function `rpe-opt` is the path option.  For example, let's get
all objects reachable by the path sequence in the last example except for `:y`
being traversed optionally now.

    (rpe m [rpe-seq :c :bar [rpe-opt :y]])
    ;=> #{{:y :bary, :x :barx}
          :bary}

**Alternative.** The function `rpe-alt` is the path alternative.  For example,
let's get all values of the `:y` key in `:foo`, `:bar`, and `:baz` submaps.

    (rpe m [rpe-seq :c [rpe-alt :foo :bar :baz] :y])
    ;=> #{:fooy :bary :bazy}

**Iteration.** The function `rpe-+` is the one-or-many path iteration, the
function `rpe-*` is the zero-or-many path iteration.

What can we reach by iterating an alternating path of `:x` and `:y` keys?

    (rpe m [rpe-+ [rpe-seq :x :y]])
    ;=> #{{:x {:y {:x {:y "Got me!"}}}}
          {:x {:y "Got me!"}}
          "Got me!"}

**Exponent.** The function `rpe-exp` is the path exponent.  It either iterates
the given path description a fixed number of times, or at least as often as the
given lower bound but at most as the given upper bound.

    (rpe m [rpe-exp 3 [rpe-seq :x :y]])
    ;=> #{"Got me!"}
    (rpe m [rpe-exp 2 19 [rpe-seq :x :y]])
    ;=> #{{:x {:y "Got me!"}}
          "Got me!"}

The iteration stops as soon as the last iteration doesn't find anything new.

**Restriction.** The function `rpe-restr` is the path restriction.  It's just
`filter` with swapped arguments, and it ensures that an ordered set is
returned.

    (rpe m [rpe-seq [rpe-+ [rpe-seq :x :y]]
                    [rpe-restr string?]])
    ;=> #{"Got me!"}

### RPEs on arbitrary Java objects

As already mentioned above, those RPEs also work on arbitrary Java objects and
Clojure data types defined with `deftype`.  For those, the "edges" are the
field names specified using keywords and the method names specified using
symbols.  For the sake of simplicity, let's discuss them using class objects
and the reflection API.

What are the superclasses of `Long`?

    (rpe Long [rpe-+ 'getSuperclass])
    ;=> #{java.lang.Number java.lang.Object}

`getSuperclass` is a method defined in the class `Class` which returns the
classes superclass (or `nil` for `Object`), and by iterating such an "edge" one
or many times, we get all superclasses.

What about all supertypes, e.g., classes and interfaces?  We simply use an
alternative.

    (rpe Long [rpe-+ [rpe-alt 'getSuperclass 'getInterfaces]])
    ;=> #{java.lang.Number java.lang.Comparable
          java.lang.Object java.io.Serializable}

What's the set of return types of all methods in the class `Long`?

    (rpe Long [rpe-seq 'getMethods 'getReturnType])
    ;=> #{int boolean java.lang.String long java.lang.Long
          byte short float double void java.lang.Class}

What if we only want to recognize getter methods?

    (rpe Long [rpe-seq 'getMethods
                       [rpe-restr #(re-matches #"^get.*" (.getName %))]
                       'getReturnType])
    ;=> #{java.lang.Long java.lang.Class}

What if we want to check both `Long` and `String`?

    (rpe [Long String]
         [rpe-seq 'getMethods
                  [rpe-restr #(re-matches #"^get.*" (.getName %))]
                  'getReturnType])
    ;=> #{java.lang.Long java.lang.Class void [B}

The `void` is strange for a getter, but there's in fact a 
`String.getBytes(...)` method that returns nothing...

Since I cannot find any standard Java classes with public fields, let's
consider this simple Clojure type with a `val` field.  In RPEs, fields are
accessed using keywords.

    (defprotocol Successor
      (succ [this]))
    
    (deftype Int [val]
      Successor
      (succ [_]
        (Int. (inc val))))

So what do we get when we start with the `Int` zero and traverse 10 `succ`
"edges" and then a `val` "edge"?

    (rpe (Int. 0) [rpe-seq [rpe-exp 10 succ] :val])
    ;=> #{10}
    
This example also demonstrated that functions of arity one like `succ` may be
used as "edges", too.  If a function throws an exception (possibly, because
it's not applicable for the object given to it, which easily happens in RPEs
with alternatives and iteration), the exception is caught, except for arity
exceptions, because those are clearly usage errors.

    (rpe 7 quot)
    ; ArityException, because quot wants 2 args
    (rpe "foo" inc)
    ;=> #{} ;; Correct arity, but simply not applicable and thus the empty set

Ok, that's all.  Have fun!

## License

Copyright (C) 2012 Tassilo Horn <tsdh80@googlemail.com>

Distributed under the Eclipse Public License, the same as Clojure.

<!-- Local Variables:        -->
<!-- mode: markdown          -->
<!-- indent-tabs-mode: nil   -->
<!-- End:                    -->

