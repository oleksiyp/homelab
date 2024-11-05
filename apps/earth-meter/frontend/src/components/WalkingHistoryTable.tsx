import {WalkingHistoryApi, Configuration, GetWalkHistory200Response} from "earth-meter-api"
import {useEffect, useState} from "react";
import {Table} from "react-bootstrap";

const apiConfig = new Configuration({
    basePath: '/api/earth-meter/v1'
});

const walkingHistoryApi = new WalkingHistoryApi(apiConfig);

interface Props {
    forUser: string;
}

export const WalkingHistoryTable = (props: Props) => {
    const [history, setHistory] = useState<GetWalkHistory200Response>();
    const {forUser} = props;
    useEffect(() => {
        walkingHistoryApi.getWalkHistory({
            user: forUser
        })
            .then(setHistory)
    }, [setHistory]);

    if (history?.years === undefined) {
        return <div>Loading...</div>
    }

    if (Math.abs(history?.overallDistance || 0) < 1) {
        return <div>No data</div>
    }

    const dist = (dist: number | undefined) => {
        return dist ? Math.round(dist / 1024) + "㎞" : ""
    }

    const percent = (perc: number) => {
        return Math.round(perc * 10000) / 100 + "%"
    }
    return <>
        <Table>
            <thead>
            <tr>
                <th>Year</th>
                <th>Sum</th>
                <th>Q1</th>
                <th>Q2</th>
                <th>Q3</th>
                <th>Q4</th>
            </tr>
            </thead>
            <tbody>
            {history.years.map(year => {
                return <tr key={year.year}>
                    <td><b>{year.year}</b></td>
                    <td>{dist(
                        (year?.quarters?.["Q1"] || 0) +
                        (year?.quarters?.["Q2"] || 0) +
                        (year?.quarters?.["Q3"] || 0) +
                        (year?.quarters?.["Q4"] || 0)
                    )}</td>
                    <td>{dist(year?.quarters?.["Q1"])}</td>
                    <td>{dist(year?.quarters?.["Q2"])}</td>
                    <td>{dist(year?.quarters?.["Q3"])}</td>
                    <td>{dist(year?.quarters?.["Q4"])}</td>
                </tr>
            })}
            </tbody>
        </Table>
        <div>Overall: {dist(history.overallDistance)} {percent((history.overallDistance || 0)/40075000.0)}</div>
    </>
}