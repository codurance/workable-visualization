(ns workable-reagent.config)

(def default-config
  {:subdomain (System/getenv "WORKABLE_SUBDOMAIN")
   :token (System/getenv "WORKABLE_TOKEN")})

