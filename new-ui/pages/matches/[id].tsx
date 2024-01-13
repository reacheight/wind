import { useRouter } from "next/router";
import { getUserContext } from "../../components/UserContextWrapper";
import { useEffect, useState } from "react";
import Routes from "../../api/routs";
import Match from "../../models/Match";
import styles from "../../styles/Match.module.css"
import Matchup from "../../components/Matchup";
import { AnalysisResult } from "../../models/AnalysisResult";
import { Button } from "@chakra-ui/button";
import { Spinner } from "@chakra-ui/spinner";
import { Team } from "../../models/Team";
import AnalysisComponent from "../../components/AnalysisComponent";
import Image from "next/image";

const Match = () => {
  let userContext = getUserContext()
  if (userContext.loading || userContext.user === null)
    return <div>Log in to view the match.</div>

  const router = useRouter()
  const { id: matchId, fromHeroView } = router.query

  const [match, setMatch] = useState<Match>(null)
  const [analysis, setAnalysis] = useState<AnalysisResult>(null)
  const [analysisLoading, setAnalysisLoading] = useState<boolean>(true)
  const [isError, setIsError] = useState<boolean>(false)
  const analysisButtonDisabled = process.env.NEXT_PUBLIC_ANALYSIS_BUTTON_DISABLED === 'true';

  const startAnalysis = async () => {
    setAnalysisLoading(true)
    setIsError(false)

    let analyzeResponse = fetch(Routes.Analysis.start(matchId), { method: 'POST' })
    await pollAnalysisRequest(analyzeResponse)
  }

  const startAnalysisFromFile = async (e) => {
    setAnalysisLoading(true)
    setIsError(false)

    let formData = new FormData()
    formData.append('replay', e.target.files[0])
    let analyzeResponse = fetch(Routes.Analysis.startFromFile(matchId), { body: formData, method: 'POST' })
    await pollAnalysisRequest(analyzeResponse);
  }

  const pollAnalysisRequest = async (analyzeResponse: Promise<Response>) => {
    let response = await analyzeResponse;
    if (!response.ok) {
      setIsError(true)
      setAnalysisLoading(false)
      return
    }

    let timer = setInterval(async () => {
      let stateResponse = await fetch(Routes.Analysis.state(matchId))
      if (!stateResponse.ok) {
        setIsError(true)
        setAnalysisLoading(false)
        return
      }

      let state = await stateResponse.json()
      if (state === 1) {
        let analysisResponse = await fetch(Routes.Analysis.get(matchId))
        setAnalysis(await analysisResponse.json())
        setAnalysisLoading(false)
        clearInterval(timer)
      } else if (state === 2) {
        setAnalysisLoading(false)
        setIsError(true)
        clearInterval()
      }
    }, 5000)
  }

  useEffect(() => {
    fetch(Routes.Matches.get(matchId))
      .then(response => response.json())
      .then(json => setMatch(json))
  }, [])

  useEffect(() => {
    fetch(Routes.Analysis.get(matchId))
      .then(response => response.json())
      .then(json => setAnalysis(json))
      .catch(() => null)
      .finally(() => setAnalysisLoading(false))
  }, [])

  if (match == null)
    return null

  const userPlayer = match.players.find(p => p.steamAccountId === userContext.user.id)
  const targetHero = fromHeroView === undefined
    ? userPlayer === undefined ? match.players[0].heroId : userPlayer.heroId
    : +fromHeroView

  const targetTeam = match.players.find(p => p.heroId === targetHero).isRadiant ? Team.Radiant : Team.Dire

  return (
    <div>
      <Matchup match={match}/>
      <div className={styles.stratz}>
        <a href={"https://stratz.com/matches/" + match.id} target={"_blank"}>
          <Button fontSize={'14px'} fontWeight={'600'}>
            More match stats on STRATZ<span>&nbsp;</span>
            <Image src={"/stratz.png"} width={16} height={16}/>
          </Button>
        </a>
      </div>
      {analysis && <AnalysisComponent heroes={match.players.map(p => p.heroId)} targetHero={targetHero} targetTeam={targetTeam} analysis={analysis.analysis} matchInfo={analysis.matchInfo} />}
      <div className={styles.centered}>
        {analysisLoading && <Spinner />}
        {(!analysisLoading && !analysis) &&
            <div>
                <div>
                    <Button fontSize={'18px'} onClick={() => startAnalysis()} isDisabled={analysisButtonDisabled}>Analyze</Button>
                </div>
                {analysisButtonDisabled && <div className={styles.replayAnalysisWarning}>
                    Automatic replay fetching is currently not available.<br />
                    You can download the replay file of the match in the Dota 2 client and upload it manually.
                </div>}
                <br />
                <div>
                    <input id="upload-file-btn" type="file" onChange={e => startAnalysisFromFile(e)} hidden={true} />
                    <label className={styles.uploadReplayButton} htmlFor="upload-file-btn">Upload replay</label>
                </div>
            </div>
        }
        {isError && <div>Something's went wrong.</div>}
      </div>
    </div>
  )
}

export default Match