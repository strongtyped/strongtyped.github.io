    def tuple[Z, Y, X, W, V]: Z => Y => X => W => V => V ** W ** X ** Y ** Z =
      z => y => x => w => v => ((((v, w), x), y), z)

    def program[F[_]: Res](implicit id_sub_f: Identity <= F,
      supplier_sub_f: Supplier <= F,
      withFile_sub_f: WithFile <= F): Free[F, Int ** Int ** Int ** String ** String] = {
      val supply1 = supply({ randomVerboseSleep("supplier")(3000); 1 })
      val supply2 = supply({ randomVerboseSleep("supplier")(3000); 2 })
      val supply3 = supply({ randomVerboseSleep("supplier")(3000); 3 })
      val readFooFile = readFile(fooFilePath)
      val readBarFile = readFile(barFilePath)
      supply1 and {
        supply2 and {
          supply3 and {
            readFooFile and {
              readBarFile end {
                tuple
              }
            }
          }
        }
      }

    }

    val future: Future[Int ** Int ** Int ** String ** String] =
      program[({ type λ[ω] = Identity[ω] ++ Supplier[ω] ++ WithFile[ω] })#λ].fold

    println(future(es).get)

    es.shutdown()
