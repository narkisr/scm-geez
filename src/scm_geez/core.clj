(ns scm-geez.core
  "Managing git cli actions quickly"
  (:require
   [clojure.string :as str]
   [clojure.java.shell :as shell]))

(defn stderr [out err]
  (binding [*out* *err*]
    (println err out)))

(defn git [& args]
  (let [{:keys [exit out err]} (apply shell/sh "git" args)]
    (when-not (= exit 0)
      (stderr out err)
      (System/exit 1))
    out))

(defn short-format
  "https://git-scm.com/docs/git-status#_short_format"
  []
  {" A" :not-updated
   " M" :not-updated
   " D" :not-updated
   "MM" :update-in-index
   "MD" :update-in-index
   "AM" :update-in-index
   "AD" :update-in-index
   "D " :deleted-from-index
   "RM" :renamed-in-index
   "RD" :renamed-in-index})

(defn status [line]
  {:state (apply str (take 2 line)) :file (apply str (drop 3 line))})

(defn gs
  "Show the current changed files for CWD numbered"
  []
  (let [status (group-by :state (map status (str/split (git "status" "--porcelain=v1") #"\n")))]))

(comment
  " X          Y     Meaning
-------------------------------------------------
	 [AMD]   not updated
M        [ MD]   updated in index
A        [ MD]   added to index
D                deleted from index
R        [ MD]   renamed in index
C        [ MD]   copied in index
[MARC]           index and work tree matches
[ MARC]     M    work tree changed since index
[ MARC]     D    deleted in work tree
[ D]        R    renamed in work tree
[ D]        C    copied in work tree
-------------------------------------------------
D           D    unmerged, both deleted
A           U    unmerged, added by us
U           D    unmerged, deleted by them
U           A    unmerged, added by them
D           U    unmerged, deleted by us
A           A    unmerged, both added
U           U    unmerged, both modified
-------------------------------------------------
?           ?    untracked
!           !    ignored
------------------------------------------------- ")
